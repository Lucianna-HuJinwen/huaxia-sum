package com.liam.common.core.utils;

import com.liam.common.core.constants.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Map;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-25
 * @Description:
 * @Version: 1.0
 */

public class JwtUtils {
    /**
     * ⽣成令牌
     *
     * @param claims 数据
     * @param secret 密钥
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims, String secret) {
        String token =
                Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512,
                        secret).compact();
        return token;
    }

    /**
     * 从令牌中获取数据
     *
     * @param token  令牌
     * @param secret 密钥
     * @return 数据
     */
    public static Claims parseToken(String token, String secret) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public static String getUserKey(Claims claims) {
        return toStr(claims.get(JwtConstants.LOGIN_USER_KEY));
    }

    public static String getUserId(Claims claims) {
        return toStr(claims.get(JwtConstants.LOGIN_USER_ID ));
    }

    private static String toStr(Object value) {
        if(value == null) {
            return "";
        }
        return value.toString();
    }


}
