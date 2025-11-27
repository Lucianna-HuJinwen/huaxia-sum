package config;

import interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-27
 * @Description:
 * @Version: 1.0
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .excludePathPatterns("/**/login", "/**/test/**") // 登录请求不会携带token
                .addPathPatterns("/**");
    }
}
