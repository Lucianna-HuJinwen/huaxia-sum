package com.liam.term.domain.template.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TemplateEditDTO extends TemplateAddDTO {

    private Long templateId;
}
