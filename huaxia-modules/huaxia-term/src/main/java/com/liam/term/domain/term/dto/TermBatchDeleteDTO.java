package com.liam.term.domain.term.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 术语批量删除DTO
 *
 * @author liam
 */
@Data
public class TermBatchDeleteDTO {

    /**
     * 术语库ID
     */
    @NotNull(message = "术语库ID不能为空")
    private Long glossaryId;

    /**
     * 术语ID列表
     */
    @NotEmpty(message = "术语ID列表不能为空")
    private List<Long> termIds;
} 