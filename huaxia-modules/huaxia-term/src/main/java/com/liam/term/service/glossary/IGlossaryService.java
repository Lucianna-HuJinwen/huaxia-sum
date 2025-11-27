package com.liam.term.service.glossary;

import com.liam.term.domain.glossary.dto.GlossaryAddDTO;
import com.liam.term.domain.glossary.dto.GlossaryEditDTO;
import com.liam.term.domain.glossary.dto.GlossaryQueryDTO;
import com.liam.term.domain.glossary.vo.GlossaryDetailVO;
import com.liam.term.domain.glossary.vo.GlossaryVO;

import java.util.List;

public interface IGlossaryService {

    List<GlossaryVO> list(GlossaryQueryDTO glossaryQueryDTO);

    String create(GlossaryAddDTO glossaryAddDTO);

    GlossaryDetailVO detail(Long glossaryId);

    int edit(GlossaryEditDTO glossaryEditDTO);

    int delete(Long glossaryId);
}
