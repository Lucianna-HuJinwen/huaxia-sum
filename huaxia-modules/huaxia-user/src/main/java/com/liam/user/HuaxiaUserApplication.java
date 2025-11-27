package com.liam.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-19
 * @Description:
 * @Version: 1.0
 */

@SpringBootApplication(scanBasePackages = {"com.liam"})
//@MapperScan("com.liam.**.mapper")
public class HuaxiaUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(HuaxiaUserApplication.class, args);
    }
}
