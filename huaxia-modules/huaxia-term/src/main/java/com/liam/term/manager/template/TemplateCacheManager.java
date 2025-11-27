package com.liam.term.manager.template;

import com.liam.common.core.constants.CacheConstants;
import com.liam.redis.service.RedisService;
import com.liam.term.domain.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 模板缓存管理器
 */
@Slf4j
@Component
public class TemplateCacheManager {

    @Autowired
    private RedisService redisService;

    /**
     * 缓存模板详情
     */
    public void setCache(Long templateId, Template template) {
        String cacheKey = CacheConstants.TEMPLATE_CACHE_KEY + templateId;
        redisService.setCacheObject(cacheKey, template, CacheConstants.TEMPLATE_CACHE_TTL, TimeUnit.SECONDS);
        log.debug("缓存模板详情: templateId={}, templateName={}", templateId, template.getTemplateName());
    }

    /**
     * 获取缓存的模板详情
     */
    public Template getCache(Long templateId) {
        String cacheKey = CacheConstants.TEMPLATE_CACHE_KEY + templateId;
        Template template = redisService.getCacheObject(cacheKey, Template.class);
        if (template != null) {
            log.debug("从缓存获取模板详情: templateId={}, templateName={}", templateId, template.getTemplateName());
        }
        return template;
    }

    /**
     * 删除模板缓存
     */
    public void deleteCache(Long templateId) {
        String cacheKey = CacheConstants.TEMPLATE_CACHE_KEY + templateId;
        redisService.deleteObject(cacheKey);
        log.debug("删除模板缓存: templateId={}", templateId);
    }

    /**
     * 清除用户的所有模板缓存
     */
    public void clearUserCache(Long userId) {
        // TODO 这里可以根据需要实现清除用户所有模板缓存的逻辑
        // 由于模板是用户私有的，可以考虑在用户相关操作时清除
        log.debug("清除用户模板缓存: userId={}", userId);
    }
} 