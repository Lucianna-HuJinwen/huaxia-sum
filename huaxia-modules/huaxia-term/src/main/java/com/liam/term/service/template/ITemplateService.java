package com.liam.term.service.template;


import com.liam.term.domain.template.dto.TemplateAddDTO;
import com.liam.term.domain.template.dto.TemplateEditDTO;
import com.liam.term.domain.template.dto.TemplateQueryDTO;
import com.liam.term.domain.template.vo.TemplateDetailVO;
import com.liam.term.domain.template.vo.TemplateVO;

import java.util.List;

public interface ITemplateService {

    List<TemplateVO> list(TemplateQueryDTO templateQueryDTO);

    String create(TemplateAddDTO templateAddDTO);

    TemplateDetailVO detail(Long templateId);

    int edit(TemplateEditDTO templateEditDTO);

    int delete(Long templateId);
}
