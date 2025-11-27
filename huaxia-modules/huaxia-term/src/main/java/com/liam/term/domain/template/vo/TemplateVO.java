package com.liam.term.domain.template.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class TemplateVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                     // 模板ID

    private String templateName; // 模板名称

    private String textType;     // 文本类型

    private String style;        // 风格

    private String purpose;      // 用途

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;   // 创建时间（格式化后的字符串，如：2025-07-27 09:00:00）
}
