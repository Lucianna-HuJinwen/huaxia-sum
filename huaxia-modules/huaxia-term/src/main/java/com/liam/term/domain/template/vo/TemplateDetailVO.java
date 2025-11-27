package com.liam.term.domain.template.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class TemplateDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                     // 模板ID

    private String templateName; // 模板名称

    private String era;          // 时代背景
    private String author;       // 作者
    private String textType;     // 文本类型
    private String style;        // 风格
    private String purpose;      // 用途
    private String audience;     // 受众
    private String translatorRole; // 译者角色
    private String scene;        // 场景
    private String customRules;  // 自定义规则（长文本）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;   // 创建时间（格式化）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String updateTime;   // 更新时间（格式化）
}