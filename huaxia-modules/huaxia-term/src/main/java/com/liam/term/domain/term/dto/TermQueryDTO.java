package com.liam.term.domain.term.dto;


import com.liam.common.core.domain.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TermQueryDTO extends PageQueryDTO {

    private String keyword;

    private Long glossaryId;
}
