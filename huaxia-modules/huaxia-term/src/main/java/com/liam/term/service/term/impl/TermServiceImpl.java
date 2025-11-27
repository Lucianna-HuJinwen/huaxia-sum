package com.liam.term.service.term.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liam.common.core.constants.Constants;
import com.liam.common.core.constants.RabbitMQConstants;
import com.liam.common.core.domain.TableDataInfo;
import com.liam.common.core.enums.ResultCode;
import com.liam.common.core.message.AutomatonCacheClearMessage;
import com.liam.common.core.utils.ThreadLocalUtil;
import com.liam.easyexcel.utils.ExcelUtils;
import com.liam.term.domain.es.TermDictES;
import com.liam.term.domain.glossary.Glossary;
import com.liam.term.domain.term.TermDict;
import com.liam.term.domain.term.dto.*;
import com.liam.term.domain.term.vo.TermBatchAddVO;
import com.liam.term.domain.term.vo.TermBatchDeleteVO;
import com.liam.term.domain.term.vo.TermVO;
import com.liam.term.elasticsearch.TermDictRepository;
import com.liam.term.manager.term.TermCacheManager;
import com.liam.term.mapper.glossary.GlossaryMapper;
import com.liam.term.mapper.term.TermDictMapper;
import com.liam.term.service.term.ITermService;
import exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class TermServiceImpl implements ITermService {

    @Autowired
    private TermDictMapper termDictMapper;

    @Autowired
    private GlossaryMapper glossaryMapper;

    @Autowired
    private TermDictRepository termDictRepository;

    @Autowired
    private TermCacheManager termCacheManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;



    @Override
    public boolean add(TermAddDTO termAddDTO) {
        // 验证术语库编辑权限（只有拥有编辑权限的用户才能添加术语）
        Glossary glossary = validateGlossaryEditAccess(termAddDTO.getGlossaryId());
        
        // 检查术语库中是否已存在相同的源术语
        List<TermDict> termList = termDictMapper.selectList(new LambdaQueryWrapper<TermDict>()
                .eq(TermDict::getGlossaryId, termAddDTO.getGlossaryId())
                .eq(TermDict::getSourceTerm, termAddDTO.getSourceTerm()));
        if (CollectionUtil.isNotEmpty(termList)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        
        // 对象转换 DTO -> Entity
        TermDict termDict = new TermDict();
        BeanUtil.copyProperties(termAddDTO, termDict);
        int insert = termDictMapper.insert(termDict);
        if (insert <= 0) {
            return false;
        }
        
        // 同步到ES
        TermDictES termDictES = new TermDictES();
        BeanUtil.copyProperties(termDict, termDictES);
        termDictRepository.save(termDictES);
        
        // 更新缓存
        termCacheManager.addCache(termDict.getTermId());
        
        // 发送自动机缓存清理消息
        sendAutomatonCacheClearMessage(termDict.getGlossaryId(), glossary.getUserId(), "add");
        
        return true;
    }

    @Override
    public int edit(TermEditDTO termEditDTO) {
        TermDict oldTerm = getTermDict(termEditDTO.getTermId());
        
        // 验证术语库编辑权限（只有拥有编辑权限的用户才能编辑术语）
        validateGlossaryEditAccess(oldTerm.getGlossaryId());
        
        oldTerm.setSourceTerm(termEditDTO.getSourceTerm());
        oldTerm.setTargetTerm(termEditDTO.getTargetTerm());
        
        // 同步到ES
        TermDictES termDictES = new TermDictES();
        BeanUtil.copyProperties(oldTerm, termDictES);
        termDictRepository.save(termDictES);
        
        int result = termDictMapper.updateById(oldTerm);
        
        // 发送自动机缓存清理消息
        if (result > 0) {
            Glossary glossary = glossaryMapper.selectById(oldTerm.getGlossaryId());
            if (glossary != null) {
                sendAutomatonCacheClearMessage(oldTerm.getGlossaryId(), glossary.getUserId(), "edit");
            }
        }
        
        return result;
    }

    @Override
    public int delete(Long termId) {
        TermDict termDict = getTermDict(termId);
        
        // 验证术语库编辑权限（只有拥有编辑权限的用户才能删除术语）
        validateGlossaryEditAccess(termDict.getGlossaryId());
        
        termDictRepository.deleteById(termId);
        termCacheManager.deleteCache(termDict.getTermId());
        
        int result = termDictMapper.deleteById(termId);
        
        // 发送自动机缓存清理消息
        if (result > 0) {
            Glossary glossary = glossaryMapper.selectById(termDict.getGlossaryId());
            if (glossary != null) {
                sendAutomatonCacheClearMessage(termDict.getGlossaryId(), glossary.getUserId(), "delete");
            }
        }
        
        return result;
    }

    @Override
    public TableDataInfo list(TermQueryDTO termQueryDTO) {
        Long glossaryId = termQueryDTO.getGlossaryId();
        
        // 检查ES中是否有数据，如果没有则同步
        long count = glossaryId != null ? 
            termDictRepository.countByGlossaryId(glossaryId) : 
            termDictRepository.count();
        if (count <= 0) {
            refreshTermDict(glossaryId);
        }
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(termQueryDTO.getPageNum() - 1, termQueryDTO.getPageSize(), sort);

        String keyword = termQueryDTO.getKeyword();
        Page<TermDictES> termDictESPage;
        
        if (StrUtil.isNotBlank(keyword) && glossaryId != null) {
            // 关键词 + 术语库过滤
            termDictESPage = termDictRepository.findByKeywordAndGlossaryId(keyword, glossaryId, pageable);
        } else if (StrUtil.isNotBlank(keyword)) {
            // 仅关键词搜索
            termDictESPage = termDictRepository.findByKeyword(keyword, pageable);
        } else if (glossaryId != null) {
            // 仅术语库过滤
            termDictESPage = termDictRepository.findByGlossaryId(glossaryId, pageable);
        } else {
            // 查询所有
            termDictESPage = termDictRepository.findAll(pageable);
        }

        long total = termDictESPage.getTotalElements();
        if (total <= 0) {
            return TableDataInfo.empty();
        }
        
        List<TermDictES> termDictESList = termDictESPage.getContent();
        List<TermVO> termVOList = BeanUtil.copyToList(termDictESList, TermVO.class);
        return TableDataInfo.success(termVOList, total);
    }

    private TermDict getTermDict(Long termId) {
        TermDict termDict = termDictMapper.selectById(termId);
        if (termDict == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return termDict;
    }

    private Glossary validateGlossaryAccess(Long glossaryId) {
        Glossary glossary = glossaryMapper.selectById(glossaryId);
        if (glossary == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        
        // 获取当前用户ID（从ThreadLocal中获取）
        Long currentUserId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if (currentUserId == null) {
            throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
        }
        
        // 检查用户是否有权限访问此术语库
        // 1. 用户自己创建的术语库
        if (currentUserId.equals(glossary.getUserId())) {
            return glossary;
        }
        
        // 2. 公开术语库（任何用户都可以访问）
        if (glossary.getIsPublic() != null && glossary.getIsPublic() == 1) {
            return glossary;
        }
        
        // 其他情况无权限访问
        throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
    }

    /**
     * 验证用户对术语库的编辑权限
     * 只有术语库的创建者或管理员（对于公开术语库）才能编辑
     */
    private Glossary validateGlossaryEditAccess(Long glossaryId) {
        Glossary glossary = glossaryMapper.selectById(glossaryId);
        if (glossary == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        
        Long currentUserId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if (currentUserId == null) {
            throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
        }
        
        // 私有术语库，只有创建者可以编辑
        if (glossary.getIsPublic() != null && glossary.getIsPublic() == 0) {
            if (!currentUserId.equals(glossary.getUserId())) {
                throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
            }
            return glossary;
        }
        
        // 公开术语库，需要检查用户是否为管理员
        if (glossary.getIsPublic() != null && glossary.getIsPublic() == 1) {
            // 这里需要根据您的系统实际情况判断用户是否为管理员
            // 暂时允许创建者编辑（即管理员创建的公开术语库）
            if (!currentUserId.equals(glossary.getUserId())) {
                throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
            }
            return glossary;
        }
        
        throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
    }

    private void refreshTermDict(Long glossaryId) {
        LambdaQueryWrapper<TermDict> wrapper = new LambdaQueryWrapper<TermDict>();
        if (glossaryId != null) {
            wrapper.eq(TermDict::getGlossaryId, glossaryId);
        }
        
        List<TermDict> termDictList = termDictMapper.selectList(wrapper);
        if (CollectionUtil.isEmpty(termDictList)) {
            return;
        }
        
        List<TermDictES> termDictESList = BeanUtil.copyToList(termDictList, TermDictES.class);
        termDictRepository.saveAll(termDictESList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TermBatchAddVO batchAdd(TermBatchAddDTO termBatchAddDTO) {
        try {
            // 验证术语库编辑权限（只有拥有编辑权限的用户才能批量添加术语）
            validateGlossaryEditAccess(termBatchAddDTO.getGlossaryId());
            
            // 定义必需的列头
            List<String> requiredHeaders = Arrays.asList("sourceTerm", "targetTerm");
            
            // 计数器
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            
            // 读取Excel文件
            ExcelUtils.ExcelParseResult<Map<String, String>> result =
                ExcelUtils.readExcelWithDynamicHeaders(
                    termBatchAddDTO.getFile().getInputStream(),
                    requiredHeaders,
                    rowData -> {
                        try {
                            String sourceTerm = rowData.get("sourceterm");
                            String targetTerm = rowData.get("targetterm");
                            
//                            log.info("处理术语数据: sourceTerm={}, targetTerm={}", sourceTerm, targetTerm);
                            
                            // 验证数据
                            if (StrUtil.isBlank(sourceTerm) || StrUtil.isBlank(targetTerm)) {
                                log.warn("术语数据为空，跳过: sourceTerm={}, targetTerm={}", sourceTerm, targetTerm);
                                failCount.incrementAndGet();
                                return;
                            }
                            
                            // 检查是否已存在相同的源术语
                            List<TermDict> existingTerms = termDictMapper.selectList(
                                new LambdaQueryWrapper<TermDict>()
                                    .eq(TermDict::getGlossaryId, termBatchAddDTO.getGlossaryId())
                                    .eq(TermDict::getSourceTerm, sourceTerm)
                            );
                            
                            if (CollectionUtil.isNotEmpty(existingTerms)) {
//                                log.warn("术语已存在，跳过: sourceTerm={}", sourceTerm);
                                failCount.incrementAndGet();
                                return;
                            }
                            
                            // 创建术语对象
                            TermDict termDict = new TermDict();
                            termDict.setGlossaryId(termBatchAddDTO.getGlossaryId());
                            termDict.setSourceTerm(sourceTerm);
                            termDict.setTargetTerm(targetTerm);
                            
                            // 保存到数据库
                            int insert = termDictMapper.insert(termDict);
                            if (insert > 0) {
//                                log.info("术语保存成功: termId={}, sourceTerm={}", termDict.getTermId(), sourceTerm);
                                // 同步到ES
                                TermDictES termDictES = new TermDictES();
                                BeanUtil.copyProperties(termDict, termDictES);
                                termDictRepository.save(termDictES);
                                
                                // 更新缓存
                                termCacheManager.addCache(termDict.getTermId());
                                
                                successCount.incrementAndGet();
                            } else {
                                log.error("术语保存失败: sourceTerm={}", sourceTerm);
                                failCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            log.error("处理术语数据异常: sourceTerm={}, error={}", 
                                rowData.get("sourceterm"), e.getMessage(), e);
                            failCount.incrementAndGet();
                        }
                    }
                );
            
            if (!result.isSuccess()) {
                return TermBatchAddVO.error(result.getErrorMessage());
            }
            
            // 批量添加完成后，发送自动机缓存清理消息
            if (successCount.get() > 0) {
                Glossary glossary = glossaryMapper.selectById(termBatchAddDTO.getGlossaryId());
                if (glossary != null) {
                    sendAutomatonCacheClearMessage(termBatchAddDTO.getGlossaryId(), glossary.getUserId(), "batch_add");
                }
            }
            
            return TermBatchAddVO.success(
                successCount.get() + failCount.get(),
                successCount.get(),
                failCount.get()
            );
            
        } catch (IOException e) {
            return TermBatchAddVO.error("文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            return TermBatchAddVO.error("批量导入失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TermBatchDeleteVO batchDelete(TermBatchDeleteDTO termBatchDeleteDTO) {
        try {
            // 验证术语库编辑权限（只有拥有编辑权限的用户才能批量删除术语）
            validateGlossaryEditAccess(termBatchDeleteDTO.getGlossaryId());
            
            int successCount = 0;
            int failCount = 0;
            int totalCount = termBatchDeleteDTO.getTermIds().size();
            
            for (Long termId : termBatchDeleteDTO.getTermIds()) {
                try {
                    // 获取术语信息
                    TermDict termDict = termDictMapper.selectById(termId);
                    if (termDict == null) {
                        log.warn("术语不存在，跳过删除: termId={}", termId);
                        failCount++;
                        continue;
                    }
                    
                    // 验证术语是否属于指定的术语库
                    if (!termDict.getGlossaryId().equals(termBatchDeleteDTO.getGlossaryId())) {
                        log.warn("术语不属于指定术语库，跳过删除: termId={}, glossaryId={}", 
                            termId, termBatchDeleteDTO.getGlossaryId());
                        failCount++;
                        continue;
                    }
                    
                    // 删除ES中的数据
                    termDictRepository.deleteById(termId);
                    
                    // 删除缓存
                    termCacheManager.deleteCache(termId);
                    
                    // 删除数据库中的数据
                    int deleteResult = termDictMapper.deleteById(termId);
                    if (deleteResult > 0) {
                        log.info("术语删除成功: termId={}, sourceTerm={}", termId, termDict.getSourceTerm());
                        successCount++;
                    } else {
                        log.error("术语删除失败: termId={}", termId);
                        failCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("删除术语异常: termId={}, error={}", termId, e.getMessage(), e);
                    failCount++;
                }
            }
            
            // 批量删除完成后，发送自动机缓存清理消息
            if (successCount > 0) {
                Glossary glossary = glossaryMapper.selectById(termBatchDeleteDTO.getGlossaryId());
                if (glossary != null) {
                    sendAutomatonCacheClearMessage(termBatchDeleteDTO.getGlossaryId(), glossary.getUserId(), "batch_delete");
                }
            }
            
            return TermBatchDeleteVO.success(totalCount, successCount, failCount);
            
        } catch (Exception e) {
            log.error("批量删除术语失败: error={}", e.getMessage(), e);
            return TermBatchDeleteVO.error("批量删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送自动机缓存清理消息
     */
    private void sendAutomatonCacheClearMessage(Long glossaryId, Long userId, String action) {
        try {
            AutomatonCacheClearMessage message = new AutomatonCacheClearMessage(glossaryId, userId, action);
            rabbitTemplate.convertAndSend(RabbitMQConstants.AUTOMATON_CACHE_CLEAR_QUEUE, message);
            log.info("发送自动机缓存清理消息: {}", message);
        } catch (Exception e) {
            log.warn("发送自动机缓存清理消息失败: glossaryId={}, userId={}, action={}, error={}", 
                glossaryId, userId, action, e.getMessage(), e);
        }
    }
}