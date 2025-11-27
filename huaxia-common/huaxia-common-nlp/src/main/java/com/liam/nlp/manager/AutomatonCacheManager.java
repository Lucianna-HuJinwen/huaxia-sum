package com.liam.nlp.manager;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

/**
 * 自动机缓存管理器
 * 管理Aho-Corasick自动机的缓存，避免重复构建
 */
@Slf4j
@Component
public class AutomatonCacheManager {

    /**
     * 自动机缓存（带过期时间）
     * Key: 会话ID或术语库ID列表的hash值，Value: 缓存项
     */
    private final ConcurrentHashMap<String, CacheItem<AhoCorasickDoubleArrayTrie<String>>> automatonCache = new ConcurrentHashMap<>();

    /**
     * 术语映射缓存（带过期时间）
     * Key: 会话ID或术语库ID列表的hash值，Value: 缓存项
     */
    private final ConcurrentHashMap<String, CacheItem<ConcurrentHashMap<String, String>>> termMappingCache = new ConcurrentHashMap<>();

    /**
     * 默认缓存过期时间（毫秒）
     */
    private static final long DEFAULT_CACHE_TTL = 30 * 60 * 1000; // 30分钟

    /**
     * 缓存项内部类
     */
    private static class CacheItem<T> {
        private final T data;
        private final long expireTime;

        public CacheItem(T data, long ttl) {
            this.data = data;
            this.expireTime = System.currentTimeMillis() + ttl;
        }

        public T getData() {
            return data;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 缓存自动机（使用会话ID）
     *
     * @param sessionId 会话ID
     * @param automaton 自动机对象
     */
    public void cacheAutomaton(String sessionId, AhoCorasickDoubleArrayTrie<String> automaton) {
        cacheAutomaton(sessionId, automaton, DEFAULT_CACHE_TTL);
    }

    /**
     * 缓存自动机（使用会话ID，指定TTL）
     *
     * @param sessionId 会话ID
     * @param automaton 自动机对象
     * @param ttl 过期时间（毫秒）
     */
    public void cacheAutomaton(String sessionId, AhoCorasickDoubleArrayTrie<String> automaton, long ttl) {
        if (sessionId == null || sessionId.trim().isEmpty() || automaton == null) {
            return;
        }

        CacheItem<AhoCorasickDoubleArrayTrie<String>> cacheItem = new CacheItem<>(automaton, ttl);
        automatonCache.put(sessionId, cacheItem);
        log.info("缓存自动机成功: sessionId={}, TTL={}分钟", sessionId, ttl / (60 * 1000));
    }

    /**
     * 缓存自动机（使用术语库ID列表，兼容旧接口）
     *
     * @param glossaryIds 术语库ID列表
     * @param automaton 自动机对象
     */
    public void cacheAutomaton(List<Long> glossaryIds, AhoCorasickDoubleArrayTrie<String> automaton) {
        cacheAutomaton(glossaryIds, automaton, DEFAULT_CACHE_TTL);
    }

    /**
     * 缓存自动机（使用术语库ID列表，指定TTL）
     *
     * @param glossaryIds 术语库ID列表
     * @param automaton 自动机对象
     * @param ttl 过期时间（毫秒）
     */
    public void cacheAutomaton(List<Long> glossaryIds, AhoCorasickDoubleArrayTrie<String> automaton, long ttl) {
        if (glossaryIds == null || glossaryIds.isEmpty() || automaton == null) {
            return;
        }

        String cacheKey = generateCacheKey(glossaryIds);
        CacheItem<AhoCorasickDoubleArrayTrie<String>> cacheItem = new CacheItem<>(automaton, ttl);
        automatonCache.put(cacheKey, cacheItem);
        log.info("缓存自动机成功: cacheKey={}, 术语库数量={}, TTL={}分钟", cacheKey, glossaryIds.size(), ttl / (60 * 1000));
    }

    /**
     * 获取缓存的自动机（使用会话ID）
     *
     * @param sessionId 会话ID
     * @return 自动机对象，如果不存在或已过期则返回null
     */
    public AhoCorasickDoubleArrayTrie<String> getAutomaton(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }

        CacheItem<AhoCorasickDoubleArrayTrie<String>> cacheItem = automatonCache.get(sessionId);
        
        if (cacheItem != null) {
            if (cacheItem.isExpired()) {
                // 已过期，删除缓存项
                automatonCache.remove(sessionId);
                log.debug("自动机缓存已过期: sessionId={}", sessionId);
                return null;
            }
            log.debug("从缓存获取自动机: sessionId={}", sessionId);
            return cacheItem.getData();
        }
        
        return null;
    }

    /**
     * 获取缓存的自动机（使用术语库ID列表，兼容旧接口）
     *
     * @param glossaryIds 术语库ID列表
     * @return 自动机对象，如果不存在或已过期则返回null
     */
    public AhoCorasickDoubleArrayTrie<String> getAutomaton(List<Long> glossaryIds) {
        if (glossaryIds == null || glossaryIds.isEmpty()) {
            return null;
        }

        String cacheKey = generateCacheKey(glossaryIds);
        CacheItem<AhoCorasickDoubleArrayTrie<String>> cacheItem = automatonCache.get(cacheKey);
        
        if (cacheItem != null) {
            if (cacheItem.isExpired()) {
                // 已过期，删除缓存项
                automatonCache.remove(cacheKey);
                log.debug("自动机缓存已过期: cacheKey={}", cacheKey);
                return null;
            }
            log.debug("从缓存获取自动机: cacheKey={}", cacheKey);
            return cacheItem.getData();
        }
        
        return null;
    }

    /**
     * 缓存术语映射（使用会话ID）
     *
     * @param sessionId 会话ID
     * @param termMapping 术语映射（源术语 -> 目标术语）
     */
    public void cacheTermMapping(String sessionId, ConcurrentHashMap<String, String> termMapping) {
        cacheTermMapping(sessionId, termMapping, DEFAULT_CACHE_TTL);
    }

    /**
     * 缓存术语映射（使用会话ID，指定TTL）
     *
     * @param sessionId 会话ID
     * @param termMapping 术语映射（源术语 -> 目标术语）
     * @param ttl 过期时间（毫秒）
     */
    public void cacheTermMapping(String sessionId, ConcurrentHashMap<String, String> termMapping, long ttl) {
        if (sessionId == null || sessionId.trim().isEmpty() || termMapping == null) {
            return;
        }

        CacheItem<ConcurrentHashMap<String, String>> cacheItem = new CacheItem<>(termMapping, ttl);
        termMappingCache.put(sessionId, cacheItem);
        log.info("缓存术语映射成功: sessionId={}, 术语数量={}, TTL={}分钟", sessionId, termMapping.size(), ttl / (60 * 1000));
    }

    /**
     * 缓存术语映射（使用术语库ID列表，兼容旧接口）
     *
     * @param glossaryIds 术语库ID列表
     * @param termMapping 术语映射（源术语 -> 目标术语）
     */
    public void cacheTermMapping(List<Long> glossaryIds, ConcurrentHashMap<String, String> termMapping) {
        cacheTermMapping(glossaryIds, termMapping, DEFAULT_CACHE_TTL);
    }

    /**
     * 缓存术语映射（使用术语库ID列表，指定TTL）
     *
     * @param glossaryIds 术语库ID列表
     * @param termMapping 术语映射（源术语 -> 目标术语）
     * @param ttl 过期时间（毫秒）
     */
    public void cacheTermMapping(List<Long> glossaryIds, ConcurrentHashMap<String, String> termMapping, long ttl) {
        if (glossaryIds == null || glossaryIds.isEmpty() || termMapping == null) {
            return;
        }

        String cacheKey = generateCacheKey(glossaryIds);
        CacheItem<ConcurrentHashMap<String, String>> cacheItem = new CacheItem<>(termMapping, ttl);
        termMappingCache.put(cacheKey, cacheItem);
        log.info("缓存术语映射成功: cacheKey={}, 术语数量={}, TTL={}分钟", cacheKey, termMapping.size(), ttl / (60 * 1000));
    }

    /**
     * 获取缓存的术语映射（使用会话ID）
     *
     * @param sessionId 会话ID
     * @return 术语映射，如果不存在或已过期则返回null
     */
    public ConcurrentHashMap<String, String> getTermMapping(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }

        CacheItem<ConcurrentHashMap<String, String>> cacheItem = termMappingCache.get(sessionId);
        
        if (cacheItem != null) {
            if (cacheItem.isExpired()) {
                // 已过期，删除缓存项
                termMappingCache.remove(sessionId);
                log.debug("术语映射缓存已过期: sessionId={}", sessionId);
                return null;
            }
            log.debug("从缓存获取术语映射: sessionId={}, 术语数量={}", sessionId, cacheItem.getData().size());
            return cacheItem.getData();
        }
        
        return null;
    }

    /**
     * 获取缓存的术语映射（使用术语库ID列表，兼容旧接口）
     *
     * @param glossaryIds 术语库ID列表
     * @return 术语映射，如果不存在或已过期则返回null
     */
    public ConcurrentHashMap<String, String> getTermMapping(List<Long> glossaryIds) {
        if (glossaryIds == null || glossaryIds.isEmpty()) {
            return null;
        }

        String cacheKey = generateCacheKey(glossaryIds);
        CacheItem<ConcurrentHashMap<String, String>> cacheItem = termMappingCache.get(cacheKey);
        
        if (cacheItem != null) {
            if (cacheItem.isExpired()) {
                // 已过期，删除缓存项
                termMappingCache.remove(cacheKey);
                log.debug("术语映射缓存已过期: cacheKey={}", cacheKey);
                return null;
            }
            log.debug("从缓存获取术语映射: cacheKey={}, 术语数量={}", cacheKey, cacheItem.getData().size());
            return cacheItem.getData();
        }
        
        return null;
    }

    /**
     * 清除指定术语库的缓存
     *
     * @param glossaryIds 术语库ID列表
     */
    public void clearCache(List<Long> glossaryIds) {
        if (glossaryIds == null || glossaryIds.isEmpty()) {
            return;
        }

        String cacheKey = generateCacheKey(glossaryIds);
        automatonCache.remove(cacheKey);
        termMappingCache.remove(cacheKey);
        log.info("清除缓存: cacheKey={}", cacheKey);
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        automatonCache.clear();
        termMappingCache.clear();
        log.info("清除所有自动机缓存");
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public String getCacheStats() {
        return String.format("自动机缓存数量: %d, 术语映射缓存数量: %d", 
                automatonCache.size(), termMappingCache.size());
    }

    /**
     * 检查术语库组合是否已缓存
     *
     * @param glossaryIds 术语库ID列表
     * @return 如果已缓存则返回缓存键，否则返回null
     */
    public String checkGlossaryCombinationCached(List<Long> glossaryIds) {
        if (glossaryIds == null || glossaryIds.isEmpty()) {
            return null;
        }

        String cacheKey = generateCacheKey(glossaryIds);
        CacheItem<AhoCorasickDoubleArrayTrie<String>> automatonItem = automatonCache.get(cacheKey);
        CacheItem<ConcurrentHashMap<String, String>> mappingItem = termMappingCache.get(cacheKey);

        if (automatonItem != null && mappingItem != null && 
            !automatonItem.isExpired() && !mappingItem.isExpired()) {
            log.debug("发现已缓存的术语库组合: cacheKey={}, 术语库数量={}", cacheKey, glossaryIds.size());
            return cacheKey;
        }

        return null;
    }

    /**
     * 检查用户的术语库组合是否已缓存
     *
     * @param userId 用户ID
     * @param glossaryIds 术语库ID列表
     * @return 如果已缓存则返回缓存键，否则返回null
     */
    public String checkUserGlossaryCombinationCached(Long userId, List<Long> glossaryIds) {
        if (userId == null || glossaryIds == null || glossaryIds.isEmpty()) {
            return null;
        }

        String cacheKey = generateUserCacheKey(userId, glossaryIds);
        CacheItem<AhoCorasickDoubleArrayTrie<String>> automatonItem = automatonCache.get(cacheKey);
        CacheItem<ConcurrentHashMap<String, String>> mappingItem = termMappingCache.get(cacheKey);

        if (automatonItem != null && mappingItem != null && 
            !automatonItem.isExpired() && !mappingItem.isExpired()) {
            log.debug("发现已缓存的用户术语库组合: cacheKey={}, 用户ID={}, 术语库数量={}", cacheKey, userId, glossaryIds.size());
            return cacheKey;
        }

        return null;
    }

    /**
     * 清除指定用户的所有自动机缓存
     *
     * @param userId 用户ID
     */
    public void clearUserCache(Long userId) {
        if (userId == null) {
            return;
        }

        String userPrefix = "user_" + userId + "_";
        final AtomicInteger automatonClearedCount = new AtomicInteger(0);
        final AtomicInteger mappingClearedCount = new AtomicInteger(0);

        // 清理自动机缓存
        automatonCache.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(userPrefix)) {
                automatonClearedCount.incrementAndGet();
                return true;
            }
            return false;
        });

        // 清理术语映射缓存
        termMappingCache.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(userPrefix)) {
                mappingClearedCount.incrementAndGet();
                return true;
            }
            return false;
        });

        if (automatonClearedCount.get() > 0 || mappingClearedCount.get() > 0) {
            log.info("清除用户自动机缓存: 用户ID={}, 自动机缓存数量={}, 术语映射缓存数量={}", 
                userId, automatonClearedCount.get(), mappingClearedCount.get());
        }
    }

    /**
     * 根据术语库组合获取会话ID
     *
     * @param glossaryIds 术语库ID列表
     * @return 会话ID，如果不存在则返回null
     */
    public String getSessionIdByGlossaryIds(List<Long> glossaryIds) {
        String cacheKey = checkGlossaryCombinationCached(glossaryIds);
        return cacheKey;
    }

    /**
     * 清理过期的缓存项
     */
    public void cleanExpiredCache() {
        int automatonExpiredCount = 0;
        int termMappingExpiredCount = 0;

        // 清理过期的自动机缓存
        for (String key : automatonCache.keySet()) {
            CacheItem<AhoCorasickDoubleArrayTrie<String>> cacheItem = automatonCache.get(key);
            if (cacheItem != null && cacheItem.isExpired()) {
                automatonCache.remove(key);
                automatonExpiredCount++;
            }
        }

        // 清理过期的术语映射缓存
        for (String key : termMappingCache.keySet()) {
            CacheItem<ConcurrentHashMap<String, String>> cacheItem = termMappingCache.get(key);
            if (cacheItem != null && cacheItem.isExpired()) {
                termMappingCache.remove(key);
                termMappingExpiredCount++;
            }
        }

        if (automatonExpiredCount > 0 || termMappingExpiredCount > 0) {
            log.info("清理过期缓存完成: 自动机缓存={}, 术语映射缓存={}", 
                    automatonExpiredCount, termMappingExpiredCount);
        }
    }

    /**
     * 生成缓存键
     *
     * @param glossaryIds 术语库ID列表
     * @return 缓存键
     */
    private String generateCacheKey(List<Long> glossaryIds) {
        // 对ID列表排序并生成hash值作为缓存键
        return glossaryIds.stream()
                .sorted()
                .map(String::valueOf)
                .reduce("", (a, b) -> a + "," + b)
                .hashCode() + "";
    }

    /**
     * 生成用户级别的缓存键
     *
     * @param userId 用户ID
     * @param glossaryIds 术语库ID列表
     * @return 用户级别的缓存键
     */
    private String generateUserCacheKey(Long userId, List<Long> glossaryIds) {
        // 对ID列表排序并生成hash值，前缀加上用户ID
        String glossaryHash = glossaryIds.stream()
                .sorted()
                .map(String::valueOf)
                .reduce("", (a, b) -> a + "," + b)
                .hashCode() + "";
        return "user_" + userId + "_" + glossaryHash;
    }
}