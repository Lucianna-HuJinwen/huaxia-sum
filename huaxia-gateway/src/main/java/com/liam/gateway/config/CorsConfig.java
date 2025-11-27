package com.liam.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * CORS跨域配置
 * 
 * @Author: LiamLMK
 * @CreateTime: 2025-01-XX
 * @Description: 配置跨域访问
 * @Version: 1.0
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源
        config.addAllowedOriginPattern("*");
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许所有请求方法
        config.addAllowedMethod("*");
        
        // 允许携带凭证
        config.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }
}
