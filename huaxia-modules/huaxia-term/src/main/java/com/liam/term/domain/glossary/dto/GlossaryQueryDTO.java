package com.liam.term.domain.glossary.dto;

import com.liam.common.core.domain.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GlossaryQueryDTO extends PageQueryDTO {

    private String title;

    private Integer isPublic;
}
