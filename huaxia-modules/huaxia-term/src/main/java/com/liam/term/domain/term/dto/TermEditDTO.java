package com.liam.term.domain.term.dto;

import lombok.Data;

@Data
public class TermEditDTO {

    private Long termId;

    private String sourceTerm;

    private String targetTerm;
}
