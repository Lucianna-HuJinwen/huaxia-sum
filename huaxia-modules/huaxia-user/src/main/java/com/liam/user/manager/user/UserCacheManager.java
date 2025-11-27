package com.liam.user.manager.user;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liam.redis.service.RedisService;
import com.liam.common.core.constants.CacheConstants;
import com.liam.user.domain.user.User;
import com.liam.user.domain.user.vo.UserVO;
import com.liam.user.domain.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-05-01
 * @Description: 操作Redis
 * @Version: 1.0
 */

@Component
public class UserCacheManager {

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    public UserVO getUserById(Long userId) {
        String userKey = getUserKey(userId);
        UserVO userVO = redisService.getCacheObject(userKey, UserVO.class);
        if (userVO != null) {
            // 将缓存延长10min
            redisService.expire(userKey, CacheConstants.USER_EXP, TimeUnit.MINUTES);
            return userVO;
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getUserId,
                        User::getNickName,
                        User::getEmail,
                        User::getStatus,
                        User::getRole)
                .eq(User::getUserId, userId));
        if (user == null) {
            return null;
        }
        refreshUser(user);
        userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    public void refreshUser(User user) {
        // 刷新用户缓存
        String userKey = getUserKey(user.getUserId());
        redisService.setCacheObject(userKey, user);
        // 设置用户缓存有效期为10分钟
        redisService.expire(userKey, CacheConstants.USER_EXP, TimeUnit.MINUTES);
    }

    // u:d:用户id
    private String getUserKey(Long userId) {
        return CacheConstants.USER_DETAIL + userId;
    }
}