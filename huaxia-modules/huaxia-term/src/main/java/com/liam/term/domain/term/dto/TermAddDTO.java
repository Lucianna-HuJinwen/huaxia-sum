package com.liam.term.domain.term.dto;

import lombok.Data;

@Data
public class TermAddDTO {

    private Long glossaryId;

    private String sourceTerm;

    private String targetTerm;
} 