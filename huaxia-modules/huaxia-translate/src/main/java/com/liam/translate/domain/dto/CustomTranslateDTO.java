package com.liam.translate.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomTranslateDTO {

    @NotBlank(message = "待翻译文本不能为空")
    private String text;

    /**
     * 会话ID，用于获取已创建的自动机
     */
    private String sessionId;

    private Long templateId;

    /**
     * 选中的术语库数量
     */
    private Integer selectedGlossaryCount;
}
