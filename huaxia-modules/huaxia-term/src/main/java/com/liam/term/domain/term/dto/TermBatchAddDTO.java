package com.liam.term.domain.term.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

/**
 * 术语批量导入DTO
 *
 * @author liam
 */
@Data
public class TermBatchAddDTO {

    /**
     * 术语库ID
     */
    @NotNull(message = "术语库ID不能为空")
    private Long glossaryId;

    /**
     * Excel文件
     */
    @NotNull(message = "Excel文件不能为空")
    private MultipartFile file;
} 