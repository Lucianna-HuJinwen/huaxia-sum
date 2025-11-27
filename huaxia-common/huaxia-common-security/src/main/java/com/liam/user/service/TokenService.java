package com.liam.user.service;

import cn.hutool.core.lang.UUID;

import com.liam.common.core.constants.CacheConstants;
import com.liam.common.core.constants.JwtConstants;
import com.liam.common.core.domain.LoginUser;
import com.liam.redis.service.RedisService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.liam.common.core.utils.JwtUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;



/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-26
 * @Description:  用户令牌登录的方法
 * @Version: 1.0
 */

@Slf4j
@Service
public class TokenService {

    @Autowired
    private RedisService redisService;

    public String createToken(Long userId, String secret, Integer identity, String nickName) {
        Map<String, Object> claims = new HashMap<>();
        String userKey = UUID.fastUUID().toString();
        claims.put(JwtConstants.LOGIN_USER_ID, userId);
        claims.put(JwtConstants.LOGIN_USER_KEY, userKey);
        String token = JwtUtils.createToken(claims, secret);
        // 第三方机制中存储敏感信息 redis

        // 表明用户身份字段 identity: 1普通用户 2管理员 对象存储

        // 数据结构 key-value  String hash（不需要存一组）list zset set
        // key 保证唯一，便于维护 -> 统一前缀：logintoken:userId（因为是雪花算法，所以可以使用id作为前缀）
        String tokenKey = getTokenKey(userKey);
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setIdentity(identity);
        loginUser.setNickName(nickName);
        // 过期时间 720分钟
        redisService.setCacheObject(tokenKey, loginUser, CacheConstants.EXPIRATION, TimeUnit.MINUTES);

        return token;
    }

    // 延长token有效时间，就是延长redis当中存储的用于身份认证的敏感信息有效时间 操作redis token -> 唯一标识

    // 在身份认证通过之后才会调用的，并且在请求到达controller层之前
    public void extendToken(Claims claims) {
//        Claims claims;
//        try {
//            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息 解析payload中信息
//            if (claims == null) {
//                log.error("解析token: {}, 出现异常, ", token);
//                return;
//            }
//        } catch (Exception e) {
//            log.error("解析token: {}, 出现异常, ", token, e);
//            return;
//        }
//        String userKey = JwtUtils.getUserKey(claims); //获取jwt中的key
        String userKey = getUserKey(claims);
        if(userKey == null) {
            return;
        }
        String tokenKey = getTokenKey(userKey);
        // 剩余180min时进行延长
        Long expire = redisService.getExpire(tokenKey, TimeUnit.MINUTES);
        if(expire != null && expire < CacheConstants.REFRESH_TIME) {
            redisService.expire(tokenKey, CacheConstants.EXPIRATION, TimeUnit.MINUTES);
        }
    }

    public void refreshLoginUser(String nickName, String email, String userKey) {
        String tokenKey = getTokenKey(userKey);
        LoginUser loginUser = redisService.getCacheObject(tokenKey, LoginUser.class);
        loginUser.setNickName(nickName);
        loginUser.setEmail(email);
        redisService.setCacheObject(tokenKey, loginUser);
    }


    private String getTokenKey(String userKey) {
        return CacheConstants.LOGIN_TOKEN_KEY + userKey;
    }

    public LoginUser getLoginUser(String token, String secret) {
        String userKey = getUserKey(token, secret);
        if(userKey == null) {
            return null;
        }
        return redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);
    }

    public boolean deleteLoginUser(String token, String secret) {
        String userKey = getUserKey(token, secret);
        if(userKey == null) {
            return false;
        }
        return redisService.deleteObject(getTokenKey(userKey));
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(JwtUtils.getUserId(claims));
    }

    public String getUserKey(Claims claims) {
        if (claims == null) return null;
        return JwtUtils.getUserKey(claims);
    }

    private String getUserKey(String token, String secret) {
        Claims claims = getClaims(token, secret);
        if (claims == null) return null;
        return JwtUtils.getUserKey(claims);
    }

    public Claims getClaims(String token, String secret) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息 解析payload中信息
            if (claims == null) {
                log.error("解析token: {}, 出现异常, ", token);
                return null;
            }
        } catch (Exception e) {
            log.error("解析token: {}, 出现异常, ", token, e);
            return null;
        }
        return claims;
    }
}
