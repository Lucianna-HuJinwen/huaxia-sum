package com.liam.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置类
 * 
 * @Author: LiamLMK
 * @CreateTime: 2025-01-25
 * @Description: 配置异步任务线程池
 * @Version: 1.0
 */
@Configuration
@EnableAsync
public class EmailConfig {

    @Value("${async.email.core-pool-size:2}")
    private int emailCorePoolSize;

    @Value("${async.email.max-pool-size:5}")
    private int emailMaxPoolSize;

    @Value("${async.email.queue-capacity:100}")
    private int emailQueueCapacity;

    @Value("${async.email.keep-alive-seconds:60}")
    private int emailKeepAliveSeconds;

    @Value("${async.email.thread-name-prefix:email-task-}")
    private String emailThreadNamePrefix;


    /**
     * 邮件发送任务线程池
     * 
     * @return 邮件任务执行器
     */
    @Bean("emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(emailCorePoolSize);
        
        // 最大线程数
        executor.setMaxPoolSize(emailMaxPoolSize);
        
        // 队列容量
        executor.setQueueCapacity(emailQueueCapacity);
        
        // 线程名前缀
        executor.setThreadNamePrefix(emailThreadNamePrefix);
        
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(emailKeepAliveSeconds);
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }

} 