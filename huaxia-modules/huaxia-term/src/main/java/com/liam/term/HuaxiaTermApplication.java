package com.liam.term;

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
public class HuaxiaTermApplication {
    public static void main(String[] args) {
        SpringApplication.run(HuaxiaTermApplication.class, args);
    }
}
