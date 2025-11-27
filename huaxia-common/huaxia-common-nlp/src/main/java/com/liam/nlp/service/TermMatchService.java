package com.liam.nlp.service;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import com.liam.nlp.manager.AutomatonCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 术语匹配服务
 * 提供基于Aho-Corasick算法的术语匹配功能
 */
@Slf4j
@Service
public class TermMatchService {

    @Autowired
    private AutomatonService automatonService;

    @Autowired
    private AutomatonCacheManager automatonCacheManager;

    /**
     * 创建或更新自动机（使用会话ID）
     *
     * @param sessionId 会话ID
     * @param sourceTerms 源术语列表
     * @param termMapping 术语映射（源术语 -> 目标术语）
     * @return 是否创建成功
     */
    public boolean createAutomaton(String sessionId, List<String> sourceTerms, 
                                   Map<String, String> termMapping) {
        if (sessionId == null || sessionId.trim().isEmpty() || 
            sourceTerms == null || sourceTerms.isEmpty() ||
            termMapping == null || termMapping.isEmpty()) {
            log.warn("创建自动机失败：参数为空");
            return false;
        }

        try {
            // 构建自动机
            AhoCorasickDoubleArrayTrie<String> automaton = automatonService.buildAutomaton(sourceTerms);
            if (automaton == null) {
                log.error("构建自动机失败");
                return false;
            }

            // 缓存自动机和术语映射
            automatonCacheManager.cacheAutomaton(sessionId, automaton);
            automatonCacheManager.cacheTermMapping(sessionId, new ConcurrentHashMap<>(termMapping));

            log.info("成功创建自动机：sessionId={}, 术语数量={}", sessionId, sourceTerms.size());
            return true;

        } catch (Exception e) {
            log.error("创建自动机过程中出现异常", e);
            return false;
        }
    }

    /**
     * 创建或更新自动机（使用术语库ID列表，兼容旧接口）
     *
     * @param glossaryIds 术语库ID列表
     * @param sourceTerms 源术语列表
     * @param termMapping 术语映射（源术语 -> 目标术语）
     * @return 是否创建成功
     */
    public boolean createAutomaton(List<Long> glossaryIds, List<String> sourceTerms, 
                                   Map<String, String> termMapping) {
        if (glossaryIds == null || glossaryIds.isEmpty() || 
            sourceTerms == null || sourceTerms.isEmpty() ||
            termMapping == null || termMapping.isEmpty()) {
            log.warn("创建自动机失败：参数为空");
            return false;
        }

        try {
            // 检查是否已存在相同的术语库组合
            String existingCacheKey = automatonCacheManager.checkGlossaryCombinationCached(glossaryIds);
            if (existingCacheKey != null) {
                log.info("发现已存在的术语库组合，跳过重复创建: cacheKey={}, 术语库数量={}", 
                    existingCacheKey, glossaryIds.size());
                return true; // 返回true表示"创建"成功（实际是已存在）
            }

            // 构建自动机
            AhoCorasickDoubleArrayTrie<String> automaton = automatonService.buildAutomaton(sourceTerms);
            if (automaton == null) {
                log.error("构建自动机失败");
                return false;
            }

            // 缓存自动机和术语映射
            automatonCacheManager.cacheAutomaton(glossaryIds, automaton);
            automatonCacheManager.cacheTermMapping(glossaryIds, new ConcurrentHashMap<>(termMapping));

            log.info("成功创建自动机：术语库数量={}, 术语数量={}", glossaryIds.size(), sourceTerms.size());
            return true;

        } catch (Exception e) {
            log.error("创建自动机过程中出现异常", e);
            return false;
        }
    }

    /**
     * 匹配文本中的术语并返回术语对（使用会话ID）
     *
     * @param sessionId 会话ID
     * @param text 待匹配的文本
     * @return 匹配到的术语对字符串，格式为"源术语 → 目标术语"，每行一个术语对
     */
    public String matchTermsAndGetPairs(String sessionId, String text) {
        if (sessionId == null || sessionId.trim().isEmpty() || 
            text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            // 获取缓存的自动机
            AhoCorasickDoubleArrayTrie<String> automaton = automatonCacheManager.getAutomaton(sessionId);
            if (automaton == null) {
                log.warn("未找到自动机缓存：sessionId={}", sessionId);
                return null;
            }

            // 获取缓存的术语映射
            ConcurrentHashMap<String, String> termMapping = automatonCacheManager.getTermMapping(sessionId);
            if (termMapping == null || termMapping.isEmpty()) {
                log.warn("未找到术语映射缓存：sessionId={}", sessionId);
                return null;
            }

            // 匹配术语
            List<String> matchedTerms = automatonService.matchTerms(automaton, text);
            if (matchedTerms.isEmpty()) {
                log.debug("文本中未匹配到任何术语");
                return null;
            }

            // 构建术语对字符串
            List<String> termPairs = new ArrayList<>();
            for (String sourceTerm : matchedTerms) {
                String targetTerm = termMapping.get(sourceTerm);
                if (targetTerm != null) {
                    termPairs.add(String.format("%s → %s", sourceTerm, targetTerm));
                }
            }

            if (termPairs.isEmpty()) {
                return null;
            }

            String result = String.join("\n", termPairs);
            log.info("术语匹配完成：sessionId={}, 匹配数量={}/{}, 文本长度={}", 
                    sessionId, termPairs.size(), matchedTerms.size(), text.length());

            return result;

        } catch (Exception e) {
            log.error("术语匹配过程中出现异常：sessionId={}", sessionId, e);
            return null;
        }
    }

    /**
     * 匹配文本中的术语并返回术语对（使用术语库ID列表，兼容旧接口）
     *
     * @param glossaryIds 术语库ID列表
     * @param text 待匹配的文本
     * @return 匹配到的术语对字符串，格式为"源术语 → 目标术语"，每行一个术语对
     */
    public String matchTermsAndGetPairs(List<Long> glossaryIds, String text) {
        if (glossaryIds == null || glossaryIds.isEmpty() || 
            text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            // 获取缓存的自动机
            AhoCorasickDoubleArrayTrie<String> automaton = automatonCacheManager.getAutomaton(glossaryIds);
            if (automaton == null) {
                log.warn("未找到自动机缓存：glossaryIds={}", glossaryIds);
                return null;
            }

            // 获取缓存的术语映射
            ConcurrentHashMap<String, String> termMapping = automatonCacheManager.getTermMapping(glossaryIds);
            if (termMapping == null || termMapping.isEmpty()) {
                log.warn("未找到术语映射缓存：glossaryIds={}", glossaryIds);
                return null;
            }

            // 匹配术语
            List<String> matchedTerms = automatonService.matchTerms(automaton, text);
            if (matchedTerms.isEmpty()) {
                log.debug("文本中未匹配到任何术语");
                return null;
            }

            // 构建术语对字符串
            List<String> termPairs = new ArrayList<>();
            for (String sourceTerm : matchedTerms) {
                String targetTerm = termMapping.get(sourceTerm);
                if (targetTerm != null) {
                    termPairs.add(String.format("%s → %s", sourceTerm, targetTerm));
                }
            }

            if (termPairs.isEmpty()) {
                return null;
            }

            String result = String.join("\n", termPairs);
            log.info("术语匹配完成：glossaryIds={}, 匹配数量={}/{}, 文本长度={}", 
                    glossaryIds, termPairs.size(), matchedTerms.size(), text.length());

            return result;

        } catch (Exception e) {
            log.error("术语匹配过程中出现异常：glossaryIds={}", glossaryIds, e);
            return null;
        }
    }

    /**
     * 检查自动机是否已缓存
     *
     * @param glossaryIds 术语库ID列表
     * @return 是否已缓存
     */
    public boolean isAutomatonCached(List<Long> glossaryIds) {
        if (glossaryIds == null || glossaryIds.isEmpty()) {
            return false;
        }
        
        AhoCorasickDoubleArrayTrie<String> automaton = automatonCacheManager.getAutomaton(glossaryIds);
        ConcurrentHashMap<String, String> termMapping = automatonCacheManager.getTermMapping(glossaryIds);
        
        return automaton != null && termMapping != null;
    }

    /**
     * 清除自动机缓存
     *
     * @param glossaryIds 术语库ID列表
     */
    public void clearAutomatonCache(List<Long> glossaryIds) {
        automatonCacheManager.clearCache(glossaryIds);
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public String getCacheStats() {
        return automatonCacheManager.getCacheStats();
    }

    /**
     * 获取自动机缓存管理器
     *
     * @return 自动机缓存管理器
     */
    public AutomatonCacheManager getAutomatonCacheManager() {
        return automatonCacheManager;
    }
}