package com.liam.translate.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateAutomatonDTO {

    @NotEmpty(message = "术语库ID列表不能为空")
    private List<String> glossaryIdList;
    
    /**
     * 会话ID（可选），如果不提供则自动生成
     */
    private String sessionId;
} 