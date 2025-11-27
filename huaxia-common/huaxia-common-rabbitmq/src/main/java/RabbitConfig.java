import com.liam.common.core.constants.RabbitMQConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-05-07
 * @Description:
 * @Version: 1.0
 */

@Configuration
public class RabbitConfig {

    @Bean
    public Queue workQueue() {
        return new Queue(RabbitMQConstants.HUAXIA_WORK_QUEUE, true);
    }
    
    @Bean
    public Queue automatonCacheClearQueue() {
        return new Queue(RabbitMQConstants.AUTOMATON_CACHE_CLEAR_QUEUE, true);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}