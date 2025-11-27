package com.liam.nlp.service;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Aho-Corasick自动机服务
 * 提供术语匹配功能
 */
@Slf4j
@Service
public class AutomatonService {

    /**
     * 构建Aho-Corasick自动机
     *
     * @param terms 术语列表
     * @return 构建好的自动机
     */
    public AhoCorasickDoubleArrayTrie<String> buildAutomaton(List<String> terms) {
        if (terms == null || terms.isEmpty()) {
            return null;
        }

        try {
            // 去重并过滤空值
            Set<String> uniqueTerms = new HashSet<>();
            for (String term : terms) {
                if (term != null && !term.trim().isEmpty()) {
                    uniqueTerms.add(term.trim());
                }
            }

            if (uniqueTerms.isEmpty()) {
                return null;
            }

            // 构建字典映射
            Map<String, String> termMap = new TreeMap<>();
            for (String term : uniqueTerms) {
                termMap.put(term, term);
            }

            // 构建自动机
            AhoCorasickDoubleArrayTrie<String> automaton = new AhoCorasickDoubleArrayTrie<>();
            automaton.build(termMap);

            log.info("成功构建Aho-Corasick自动机，术语数量: {}", uniqueTerms.size());
            return automaton;

        } catch (Exception e) {
            log.error("构建Aho-Corasick自动机失败", e);
            return null;
        }
    }

    /**
     * 使用自动机匹配文本中的术语
     *
     * @param automaton 自动机
     * @param text 待匹配的文本
     * @return 匹配到的术语列表（已去重）
     */
    public List<String> matchTerms(AhoCorasickDoubleArrayTrie<String> automaton, String text) {
        if (automaton == null || text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<AhoCorasickDoubleArrayTrie.Hit<String>> hits = automaton.parseText(text);
            
            if (hits.isEmpty()) {
                return Collections.emptyList();
            }

            // 应用最长匹配策略
            List<AhoCorasickDoubleArrayTrie.Hit<String>> filteredHits = applyLongestMatchStrategy(hits);
            
            // 提取匹配到的术语并去重
            Set<String> matchedTerms = new HashSet<>();
            for (AhoCorasickDoubleArrayTrie.Hit<String> hit : filteredHits) {
                matchedTerms.add(hit.value);
            }

            List<String> result = new ArrayList<>(matchedTerms);
            log.debug("匹配到的术语数量: {}, 原始匹配数: {}", result.size(), hits.size());
            
            return result;

        } catch (Exception e) {
            log.error("术语匹配过程中出现异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 应用最长匹配策略
     * 当多个匹配重叠时，保留最长的匹配
     *
     * @param hits 原始匹配结果
     * @return 过滤后的匹配结果
     */
    private List<AhoCorasickDoubleArrayTrie.Hit<String>> applyLongestMatchStrategy(
            List<AhoCorasickDoubleArrayTrie.Hit<String>> hits) {
        
        if (hits.size() <= 1) {
            return hits;
        }

        // 按起始位置排序
        hits.sort(Comparator.comparingInt(hit -> hit.begin));

        List<AhoCorasickDoubleArrayTrie.Hit<String>> result = new ArrayList<>();
        AhoCorasickDoubleArrayTrie.Hit<String> current = hits.get(0);

        for (int i = 1; i < hits.size(); i++) {
            AhoCorasickDoubleArrayTrie.Hit<String> next = hits.get(i);

            // 检查是否有重叠
            if (current.end > next.begin) {
                // 有重叠，保留更长的匹配
                if (next.end - next.begin > current.end - current.begin) {
                    current = next;
                }
                // 如果当前匹配更长或相等，保持current不变
            } else {
                // 没有重叠，添加当前匹配并更新
                result.add(current);
                current = next;
            }
        }

        // 添加最后一个匹配
        result.add(current);

        return result;
    }
}