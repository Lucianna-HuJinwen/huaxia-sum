package com.liam.langchain4j.domain.dto;

import lombok.Data;

@Data
public class TemplatePromptDTO {

    private String templateName;
    private String era;          // 时代背景
    private String author;       // 作者
    private String textType;     // 文本类型
    private String style;        // 风格
    private String purpose;      // 用途
    private String audience;     // 受众
    private String translatorRole; // 译者角色
    private String scene;        // 场景
    private String customRules;  // 自定义规则
}