package com.liam.term.domain.template.dto;

import com.liam.common.core.domain.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class TemplateQueryDTO extends PageQueryDTO {

    private String templateName; // 模板名称（模糊查询）
}
