package com.liam.langchain4j.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    /**
     * 创建并配置聊天记忆提供器Bean
     *
     * @return ChatMemoryProvider 聊天记忆提供器实例
     * <p>
     * 该方法返回一个ChatMemoryProvider，用于为每个对话ID创建独立的记忆窗口。
     * 每个记忆窗口将保留最近 10 条消息，实现对话历史的有限窗口管理。
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        // 为每个对话ID (@MemoryId) 创建一个独立的记忆窗口
        return memoryId -> MessageWindowChatMemory.builder()
                .maxMessages(10) // 保留最近10条消息
                .build();
    }
}
