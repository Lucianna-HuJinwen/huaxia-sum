package com.liam.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-26
 * @Description:
 * @Version: 1.0
 */

@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "security.ignore")
public class IgnoreWhiteProperties
{
    /**
     * 放⾏⽩名单配置，⽹关不校验此处的⽩名单
     */
    private List<String> whites = new ArrayList<String>() {{
        // 默认白名单
        add("/api/translate/statistics");
        add("/api/user/login");
        add("/api/user/register");
    }};
    public List<String> getWhites()
    {
        return whites;
    }
    public void setWhites(List<String> whites)
    {
        this.whites = whites;
    }
}