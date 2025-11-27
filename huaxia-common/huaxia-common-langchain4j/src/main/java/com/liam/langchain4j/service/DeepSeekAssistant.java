package com.liam.langchain4j.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * 将其视为标准的 Spring Boot @Service，但具有 AI 功能。
 * 可自动装配 (@Autowired)
 */
@AiService
public interface DeepSeekAssistant {

    /**
     * 与用户进行中华传统文化相关的对话。
     * 该方法使用系统消息限制助手只回答与中华传统文化相关的问题，
     * 并使用用户消息模板来格式化输入问题。
     *
     * @param userId 用户的唯一标识符，用于维护对话记忆
     * @param question 用户提出的问题
     * @return 返回一个 Flux 流，包含助手的回答内容
     */
    @SystemMessage("你是一位专业的中华传统文化助手，只回答与中华传统文化相关的问题。输出请保持为普通文本，不要加任何排版如Markdown等。" +
            "输出限制：对于其他领域的问题禁止回答，直接返回'抱歉，我只能回答中华传统文化相关的问题。'")
    @UserMessage("请回答以下中华传统文化问题：{{question}}")
    Flux<String> chatWithChatMemory(@MemoryId Long userId,
                                    @V("question") String question);

    /**
     * 自定义翻译功能，支持中英文互译。
     * 该方法使用系统消息定义翻译规则和要求，
     * 并使用用户消息模板来传递待翻译的文本。
     *
     * @param prompt 待翻译的文本内容
     * @return 返回翻译后的文本流
     */
    @SystemMessage("你是一位专业的中英翻译专家。请严格根据以下翻译模板和术语对照（如有），对输入文本进行翻译。\n" +
            "你必须遵守：【若原文为中文，请翻译成英文；若原文为英文，请翻译成中文。】，我再重复一遍，【若原文为中文，请翻译成英文；若原文为英文，请翻译成中文。】，不准出现中译中，或者英译英\n" +
            "注意：只输出翻译结果本身，不要添加任何解释、说明或格式修饰。")
    @UserMessage("{{prompt}}")
    String customTranslate(@V("prompt") String prompt);


    /**
     * 自定义翻译功能，支持中英文互译。
     * 该方法使用系统消息定义翻译规则和要求，
     * 并使用用户消息模板来传递待翻译的文本。
     *
     * @param prompt 待翻译的文本内容
     * @return 返回翻译后的文本流
     */
    @SystemMessage("你是一位专业的中英翻译专家。请严格根据以下翻译模板和术语对照（如有），对输入文本进行翻译。\n" +
            "你必须遵守：【若原文为中文，请翻译成英文；若原文为英文，请翻译成中文。】，我再重复一遍，【若原文为中文，请翻译成英文；若原文为英文，请翻译成中文。】，不准出现中译中，或者英译英\n" +
            "注意：只输出翻译结果本身，不要添加任何解释、说明或格式修饰。")
    @UserMessage("{{prompt}}")
    Flux<String> customTranslateFlux(@V("prompt") String prompt);
}
