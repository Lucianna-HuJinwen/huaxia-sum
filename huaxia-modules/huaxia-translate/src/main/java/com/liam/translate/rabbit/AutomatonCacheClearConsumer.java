package com.liam.translate.rabbit;

import com.liam.common.core.constants.RabbitMQConstants;
import com.liam.common.core.message.AutomatonCacheClearMessage;
import com.liam.translate.service.ITranslateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 自动机缓存清理消息监听器
 */
@Slf4j
@Component
public class AutomatonCacheClearConsumer {

    @Autowired
    private ITranslateService translateService;

    /**
     * 监听自动机缓存清理消息
     */
    @RabbitListener(queues = RabbitMQConstants.AUTOMATON_CACHE_CLEAR_QUEUE)
    public void handleAutomatonCacheClearMessage(AutomatonCacheClearMessage message) {
        try {
            log.info("接收到自动机缓存清理消息: {}", message);
            
            // 清理该术语库相关的自动机缓存
            translateService.clearGlossaryAutomatonCache(message.getGlossaryId());
            
            log.info("自动机缓存清理完成: glossaryId={}, action={}", 
                message.getGlossaryId(), message.getAction());
        } catch (Exception e) {
            log.error("处理自动机缓存清理消息异常: message={}, error={}", 
                message, e.getMessage(), e);
        }
    }
}