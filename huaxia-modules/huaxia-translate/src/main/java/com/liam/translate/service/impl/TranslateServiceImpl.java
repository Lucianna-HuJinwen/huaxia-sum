package com.liam.translate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liam.common.core.constants.Constants;
import com.liam.common.core.enums.ResultCode;
import com.liam.common.core.utils.ThreadLocalUtil;
import com.liam.langchain4j.domain.dto.TemplatePromptDTO;
import com.liam.langchain4j.service.DeepSeekAssistant;
import com.liam.langchain4j.service.PromptService;
import com.liam.nlp.service.TermMatchService;
import com.liam.redis.service.RedisService;
import com.liam.term.domain.glossary.Glossary;
import com.liam.term.domain.template.Template;
import com.liam.term.domain.term.TermDict;
import com.liam.term.mapper.glossary.GlossaryMapper;
import com.liam.term.mapper.template.TemplateMapper;
import com.liam.term.mapper.term.TermDictMapper;
import com.liam.translate.domain.dto.CustomTranslateDTO;
import com.liam.translate.service.ITranslateService;
import com.liam.translate.service.ITranslateStatisticsService;
import exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TranslateServiceImpl implements ITranslateService {

    @Autowired
    private TemplateMapper templateMapper;

    @Autowired
    private TermDictMapper termDictMapper;

    @Autowired
    private DeepSeekAssistant deepSeekAssistant;

    @Autowired
    private PromptService promptService;

    @Autowired
    private TermMatchService termMatchService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GlossaryMapper glossaryMapper;

    @Autowired
    private ITranslateStatisticsService translateStatisticsService;

    @Override
    public String customTranslate(CustomTranslateDTO customTranslateDTO) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 获取模板信息（可选）- 使用缓存
            TemplatePromptDTO templateDTO = null;
            if (customTranslateDTO.getTemplateId() != null) {
                templateDTO = getTemplateWithCache(customTranslateDTO.getTemplateId());
            }

            // 2. 智能匹配术语（可选）- 使用会话ID获取已创建的自动机
            String matchedTerms = null;
            if (customTranslateDTO.getSessionId() != null && !customTranslateDTO.getSessionId().trim().isEmpty()) {
                // 验证自动机是否存在
                boolean automatonExists = termMatchService.getAutomatonCacheManager().getAutomaton(customTranslateDTO.getSessionId()) != null;
                if (automatonExists) {
                    matchedTerms = termMatchService.matchTermsAndGetPairs(customTranslateDTO.getSessionId(), customTranslateDTO.getText());
                } else {
                    log.warn("会话ID对应的自动机不存在，跳过术语匹配: sessionId={}", customTranslateDTO.getSessionId());
                }
            }

            // 3. 拼装prompt
            String prompt = promptService.buildTranslatePrompt(
                    templateDTO,
                    customTranslateDTO.getText(),
                    matchedTerms
            );

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("翻译预处理完成: 耗时={}ms, 模板ID={}, 会话ID={}, 匹配术语数={}", 
                processingTime, 
                customTranslateDTO.getTemplateId(),
                customTranslateDTO.getSessionId(),
                matchedTerms != null ? matchedTerms.split("\n").length : 0
            );

            // 4. 调用AI进行定制化翻译
            String result = deepSeekAssistant.customTranslate(prompt);

            // 5. 记录翻译统计信息
            recordTranslationStatistics(customTranslateDTO, matchedTerms);

            return result;

        } catch (Exception e) {
            log.error("翻译过程中出现异常: error={}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Flux<String> customTranslateFlux(CustomTranslateDTO customTranslateDTO) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取模板信息（可选）- 使用缓存
            TemplatePromptDTO templateDTO = null;
            if (customTranslateDTO.getTemplateId() != null) {
                templateDTO = getTemplateWithCache(customTranslateDTO.getTemplateId());
            }

            // 2. 智能匹配术语（可选）- 使用会话ID获取已创建的自动机
            String matchedTerms = null;
            if (customTranslateDTO.getSessionId() != null && !customTranslateDTO.getSessionId().trim().isEmpty()) {
                // 验证自动机是否存在
                boolean automatonExists = termMatchService.getAutomatonCacheManager().getAutomaton(customTranslateDTO.getSessionId()) != null;
                if (automatonExists) {
                    matchedTerms = termMatchService.matchTermsAndGetPairs(customTranslateDTO.getSessionId(), customTranslateDTO.getText());
                } else {
                    log.warn("会话ID对应的自动机不存在，跳过术语匹配: sessionId={}", customTranslateDTO.getSessionId());
                }
            }

            // 3. 拼装prompt
            String prompt = promptService.buildTranslatePrompt(
                    templateDTO,
                    customTranslateDTO.getText(),
                    matchedTerms
            );

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("翻译预处理完成: 耗时={}ms, 模板ID={}, 会话ID={}, 匹配术语数={}",
                    processingTime,
                    customTranslateDTO.getTemplateId(),
                    customTranslateDTO.getSessionId(),
                    matchedTerms != null ? matchedTerms.split("\n").length : 0
            );

            // 4. 调用AI进行定制化翻译
            Flux<String> result = deepSeekAssistant.customTranslateFlux(prompt);

            // 5. 记录翻译统计信息
            recordTranslationStatistics(customTranslateDTO, matchedTerms);

            return result;

        } catch (Exception e) {
            log.error("翻译过程中出现异常: error={}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public String createAutomaton(List<String> glossaryIdList) {
        if (glossaryIdList == null || glossaryIdList.isEmpty()) {
            log.warn("创建自动机失败：术语库ID列表为空");
            return null;
        }

        try {
            // 获取当前用户ID
            Long currentUserId = getCurrentUserId(glossaryIdList);
            log.info("开始创建自动机: 用户ID={}, 术语库ID={}", currentUserId, glossaryIdList);

            // 1. 检查是否已存在相同的术语库组合（包含用户ID）
            String existingSessionId = checkExistingAutomaton(currentUserId, glossaryIdList);
            if (existingSessionId != null) {
                log.info("发现已存在的自动机，直接返回会话ID: sessionId={}, 用户ID={}, 术语库数量={}", 
                    existingSessionId, currentUserId, glossaryIdList.size());
                return existingSessionId;
            }

            // 2. 生成新的会话ID
            String sessionId = generateSessionId();

            // 3. 查询所有术语库的术语数据
            List<String> sourceTerms = new ArrayList<>();
            Map<String, String> termMapping = new HashMap<>();

            for (String glossaryIdStr : glossaryIdList) {
                Long glossaryId = Long.parseLong(glossaryIdStr);
                List<TermDict> termList = termDictMapper.selectList(
                    new LambdaQueryWrapper<TermDict>()
                        .eq(TermDict::getGlossaryId, glossaryId)
                        .select(TermDict::getSourceTerm, TermDict::getTargetTerm)
                );

                for (TermDict term : termList) {
                    if (term.getSourceTerm() != null && term.getTargetTerm() != null) {
                        sourceTerms.add(term.getSourceTerm());
                        termMapping.put(term.getSourceTerm(), term.getTargetTerm());
                    }
                }
            }

            if (sourceTerms.isEmpty()) {
                log.warn("术语库中无有效术语，无法创建自动机: 用户ID={}, glossaryIds={}", currentUserId, glossaryIdList);
                return null;
            }

            // 4. 创建自动机
            boolean success = termMatchService.createAutomaton(sessionId, sourceTerms, termMapping);
            if (success) {
                // 5. 存储术语库组合到会话ID的映射（包含用户ID）
                storeGlossarySessionMapping(currentUserId, glossaryIdList, sessionId);
                
                log.info("自动机创建成功: sessionId={}, 用户ID={}, 术语库数量={}, 术语数量={}", 
                    sessionId, currentUserId, glossaryIdList.size(), sourceTerms.size());
                return sessionId;
            } else {
                log.error("自动机创建失败");
                return null;
            }

        } catch (Exception e) {
            log.error("创建自动机过程中出现异常: glossaryIds={}", glossaryIdList, e);
            return null;
        }
    }

    /**
     * 检查是否已存在相同的术语库组合（包含用户ID）
     *
     * @param userId 用户ID
     * @param glossaryIdList 术语库ID列表
     * @return 如果已存在则返回会话ID，否则返回null
     */
    private String checkExistingAutomaton(Long userId, List<String> glossaryIdList) {
        try {
            String cacheKey = generateUserCacheKey(userId, glossaryIdList);
            String redisKey = "automaton:user:" + userId + ":glossary:" + cacheKey;
            
            // 1. 检查Redis中是否已存储该组合的会话ID
            String existingSessionId = redisService.getCacheObject(redisKey, String.class);
            if (existingSessionId != null) {
                // 2. 验证内存中是否真的存在对应的自动机
                boolean automatonExists = termMatchService.getAutomatonCacheManager().getAutomaton(existingSessionId) != null;
                
                if (automatonExists) {
                    log.debug("从Redis和内存缓存都发现已存在的自动机: sessionId={}, 用户ID={}", existingSessionId, userId);
                    return existingSessionId;
                } else {
                    // Redis中有记录但内存中没有自动机，说明应用重启了，清理Redis记录
                    log.warn("Redis中存在会话ID但内存中无自动机，清理Redis记录: sessionId={}, 用户ID={}", existingSessionId, userId);
                    redisService.deleteObject(redisKey);
                    return null;
                }
            }
            
            // 3. 检查内存缓存中是否存在该用户组合
            List<Long> longGlossaryIdList = glossaryIdList.stream()
                    .map(Long::parseLong)
                    .collect(java.util.stream.Collectors.toList());
            String userCacheKey = termMatchService.getAutomatonCacheManager().checkUserGlossaryCombinationCached(userId, longGlossaryIdList);
            if (userCacheKey != null) {
                log.debug("从内存缓存发现已存在的用户自动机组合: cacheKey={}, 用户ID={}", userCacheKey, userId);
                return userCacheKey; // 返回缓存键作为会话ID
            }
            
            // 4. 检查内存缓存中是否存在该组合（向后兼容）
            if (termMatchService.getAutomatonCacheManager().checkGlossaryCombinationCached(longGlossaryIdList) != null) {
                String legacyCacheKey = generateCacheKey(longGlossaryIdList);
                log.debug("从内存缓存发现已存在的自动机组合(兼容模式): cacheKey={}", legacyCacheKey);
                return legacyCacheKey; // 返回缓存键作为会话ID
            }
            
            return null;
        } catch (Exception e) {
            log.warn("检查已存在自动机时出现异常", e);
            return null;
        }
    }

    /**
     * 存储术语库组合到会话ID的映射（包含用户ID）
     *
     * @param userId 用户ID
     * @param glossaryIdList 术语库ID列表
     * @param sessionId 会话ID
     */
    private void storeGlossarySessionMapping(Long userId, List<String> glossaryIdList, String sessionId) {
        try {
            String cacheKey = generateUserCacheKey(userId, glossaryIdList);
            String redisKey = "automaton:user:" + userId + ":glossary:" + cacheKey;
            
            // 存储到Redis，设置30分钟过期时间
            redisService.setCacheObject(redisKey, sessionId, 30L, TimeUnit.MINUTES);
            log.debug("存储术语库组合到会话ID映射: cacheKey={}, sessionId={}, 用户ID={}", cacheKey, sessionId, userId);
        } catch (Exception e) {
            log.warn("存储术语库组合映射时出现异常", e);
        }
    }

    /**
     * 生成缓存键（向后兼容）
     */
    private String generateCacheKey(List<Long> glossaryIdList) {
        return glossaryIdList.stream()
                .sorted()
                .map(String::valueOf)
                .reduce("", (a, b) -> a + "," + b)
                .hashCode() + "";
    }

    /**
     * 生成用户级别的缓存键
     *
     * @param userId 用户ID
     * @param glossaryIdList 术语库ID列表
     * @return 用户级别的缓存键
     */
    private String generateUserCacheKey(Long userId, List<String> glossaryIdList) {
        String glossaryHash = glossaryIdList.stream()
                .sorted()
                .reduce("", (a, b) -> a + "," + b)
                .hashCode() + "";
        return "user_" + userId + "_" + glossaryHash;
    }

    /**
     * 获取当前用户ID
     * 优先从ThreadLocal获取，如果获取不到则从术语库中推断用户ID
     */
    private Long getCurrentUserId(List<String> glossaryIdList) {
        // 首先尝试从ThreadLocal获取用户ID
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if (userId != null) {
            return userId;
        }
        
        // 如果ThreadLocal中没有用户ID，则从术语库中获取第一个术语库的所有者ID
        if (glossaryIdList != null && !glossaryIdList.isEmpty()) {
            try {
                Long firstGlossaryId = Long.parseLong(glossaryIdList.get(0));
                Glossary firstGlossary = glossaryMapper.selectById(firstGlossaryId);
                if (firstGlossary != null) {
                    log.debug("从术语库推断用户ID: glossaryId={}, userId={}", firstGlossary.getGlossaryId(), firstGlossary.getUserId());
                    return firstGlossary.getUserId();
                }
            } catch (Exception e) {
                log.warn("从术语库获取用户ID失败: glossaryId={}, error={}", glossaryIdList.get(0), e.getMessage());
            }
        }
        
        // 如果都无法获取用户ID，则使用默认值（向后兼容）
        log.warn("无法获取用户ID，使用默认缓存策略");
        return null;
    }

    /**
     * 清理指定用户的所有自动机缓存
     */
    public void clearUserAutomatonCache(Long userId) {
        try {
            // 清理Redis中的用户自动机缓存
            String pattern = "automaton:user:" + userId + ":*";
            long deletedCount = redisService.deleteObjects(pattern);
            
            // 清理内存中的用户自动机缓存
            termMatchService.getAutomatonCacheManager().clearUserCache(userId);
            
            log.info("清理用户自动机缓存完成: 用户ID={}, Redis删除数量={}", userId, deletedCount);
        } catch (Exception e) {
            log.warn("清理用户自动机缓存时出现异常: 用户ID={}", userId, e);
        }
    }

    /**
     * 清理指定术语库相关的自动机缓存
     */
    public void clearGlossaryAutomatonCache(Long glossaryId) {
        try {
            // 获取术语库的所有者
            Glossary glossary = glossaryMapper.selectById(glossaryId);
            if (glossary != null) {
                // 清理该用户的所有自动机缓存
                clearUserAutomatonCache(glossary.getUserId());
                log.info("清理术语库相关自动机缓存完成: glossaryId={}, 用户ID={}", glossaryId, glossary.getUserId());
            }
        } catch (Exception e) {
            log.warn("清理术语库相关自动机缓存时出现异常: glossaryId={}", glossaryId, e);
        }
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * 使用缓存获取模板信息
     */
    private TemplatePromptDTO getTemplateWithCache(Long templateId) {
        try {
            Template template = getTemplate(templateId);
            TemplatePromptDTO templateDTO = new TemplatePromptDTO();
            BeanUtil.copyProperties(template, templateDTO);
            return templateDTO;
        } catch (Exception e) {
            log.error("获取模板失败: templateId={}", templateId, e);
            return null;
        }
    }

    private Template getTemplate(Long templateId) {
        Template template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return template;
    }

    /**
     * 记录翻译统计信息
     *
     * @param customTranslateDTO 翻译请求DTO
     * @param matchedTerms 匹配到的术语字符串
     */
    private void recordTranslationStatistics(CustomTranslateDTO customTranslateDTO, String matchedTerms) {
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserIdFromThreadLocal();
            if (userId == null) {
                log.warn("无法获取用户ID，跳过统计记录");
                return;
            }

            // 获取选中的术语库数量（从前端传递）
            Integer selectedGlossaryCount = customTranslateDTO.getSelectedGlossaryCount();
            if (selectedGlossaryCount == null) {
                selectedGlossaryCount = 0; // 如果前端没有传递，默认为0
            }

            // 计算匹配到的术语数量
            Integer matchedTermCount = 0;
            if (matchedTerms != null && !matchedTerms.trim().isEmpty()) {
                matchedTermCount = matchedTerms.split("\n").length;
            }

            // 记录统计信息
            translateStatisticsService.recordTranslationStatistics(userId, selectedGlossaryCount, matchedTermCount);

        } catch (Exception e) {
            log.error("记录翻译统计信息失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 从ThreadLocal获取当前用户ID
     */
    private Long getCurrentUserIdFromThreadLocal() {
        return ThreadLocalUtil.get(Constants.USER_ID, Long.class);
    }



}
