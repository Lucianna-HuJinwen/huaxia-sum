package com.liam.term.controller.template;

import com.liam.common.core.controller.BaseController;
import com.liam.common.core.domain.R;
import com.liam.common.core.domain.TableDataInfo;
import com.liam.term.domain.template.dto.TemplateAddDTO;
import com.liam.term.domain.template.dto.TemplateEditDTO;
import com.liam.term.domain.template.dto.TemplateQueryDTO;
import com.liam.term.domain.template.vo.TemplateDetailVO;
import com.liam.term.service.template.ITemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/template")
public class TemplateController extends BaseController {

    @Autowired
    private ITemplateService templateService;

    @GetMapping("/list")
    public TableDataInfo list(TemplateQueryDTO templateQueryDTO) {
        return getTableDataInfo(templateService.list(templateQueryDTO));
    }

    @GetMapping("/detail")
    public R<TemplateDetailVO> detail(@RequestParam Long templateId) {
        return R.ok(templateService.detail(templateId));
    }

    @PostMapping("/create")
    public R<String> create(@RequestBody TemplateAddDTO templateAddDTO) {
        return R.ok(templateService.create(templateAddDTO));
    }

    @PutMapping("/edit")
    public R<Void> edit(@RequestBody TemplateEditDTO templateEditDTO) {
        return toR(templateService.edit(templateEditDTO));
    }

    @DeleteMapping("/delete")
    public R<Void> delete(@RequestParam Long templateId) {
        return toR(templateService.delete(templateId));
    }
}
