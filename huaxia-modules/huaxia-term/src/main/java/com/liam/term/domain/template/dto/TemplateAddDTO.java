package com.liam.term.domain.template.dto;


import lombok.Data;

@Data
public class TemplateAddDTO {

    private String templateName; // 模板名称（必填）

    private String era;          // 时代背景（可选）
    private String author;       // 作者（可选）
    private String textType;     // 文本类型（可选）
    private String style;        // 风格（可选）
    private String purpose;      // 用途（可选）
    private String audience;     // 受众（可选）
    private String translatorRole; // 译者角色（可选）
    private String scene;        // 场景（可选）
    private String customRules;  // 自定义规则（可选，长文本）
}
