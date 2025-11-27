package com.liam.term.manager.term;

import com.liam.common.core.constants.CacheConstants;
import com.liam.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TermCacheManager {

    @Autowired
    private RedisService redisService;

    public void addCache(Long termId) {
        redisService.leftPushForList(CacheConstants.TERM_LIST, termId);
    }

    public void deleteCache(Long termId) {
        redisService.removeForList(CacheConstants.TERM_LIST, termId);
    }
}
