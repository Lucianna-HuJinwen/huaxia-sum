package com.liam.translate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {"com.liam"})
//@MapperScan("com.liam.**.mapper")
public class HuaxiaTranslateApplication {
    public static void main(String[] args) {
        SpringApplication.run(HuaxiaTranslateApplication.class, args);
    }
}