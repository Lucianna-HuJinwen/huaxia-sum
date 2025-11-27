package com.liam.term.controller.glossary;


import com.liam.common.core.controller.BaseController;
import com.liam.common.core.domain.R;
import com.liam.common.core.domain.TableDataInfo;
import com.liam.term.domain.glossary.dto.GlossaryAddDTO;
import com.liam.term.domain.glossary.dto.GlossaryEditDTO;
import com.liam.term.domain.glossary.dto.GlossaryQueryDTO;
import com.liam.term.domain.glossary.vo.GlossaryDetailVO;
import com.liam.term.service.glossary.IGlossaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/glossary")
public class GlossaryController extends BaseController {

    @Autowired
    private IGlossaryService glossaryService;

    @GetMapping("/list")
    public TableDataInfo list(GlossaryQueryDTO glossaryQueryDTO) {
        return getTableDataInfo(glossaryService.list(glossaryQueryDTO));
    }

    @PostMapping("/create")
    public R<String> create(@RequestBody GlossaryAddDTO glossaryAddDTO) {
        return R.ok(glossaryService.create(glossaryAddDTO));
    }

    @GetMapping("/detail")
    public R<GlossaryDetailVO> detail(Long glossaryId) {
        return R.ok(glossaryService.detail(glossaryId));
    }

    @PutMapping("/edit")
    public R<Void> edit(@RequestBody GlossaryEditDTO glossaryEditDTO) {
        return toR(glossaryService.edit(glossaryEditDTO));
    }

    @DeleteMapping("/delete")
    public R<Void> delete(Long glossaryId) {
        return toR(glossaryService.delete(glossaryId));
    }

}
