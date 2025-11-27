package com.liam.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-19
 * @Description:
 * @Version: 1.0
 */


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}) // 排除数据源
public class HuaxiaGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(HuaxiaGatewayApplication.class, args);
    }
}
