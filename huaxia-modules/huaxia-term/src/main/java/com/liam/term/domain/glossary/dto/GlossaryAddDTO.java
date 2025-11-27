package com.liam.term.domain.glossary.dto;

import lombok.Data;

@Data
public class GlossaryAddDTO {

    private String title;

    private String sourceLanguage;

    private String targetLanguage;

    private String description;
}
