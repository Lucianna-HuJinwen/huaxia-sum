package com.liam.term.domain.glossary.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GlossaryEditDTO extends GlossaryAddDTO {

    private Long glossaryId;
}
