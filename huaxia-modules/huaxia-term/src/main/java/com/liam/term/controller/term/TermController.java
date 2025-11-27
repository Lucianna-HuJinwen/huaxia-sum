package com.liam.term.controller.term;

import com.liam.common.core.controller.BaseController;
import com.liam.common.core.domain.R;
import com.liam.common.core.domain.TableDataInfo;
import com.liam.term.domain.term.dto.*;
import com.liam.term.domain.term.vo.TermBatchAddVO;
import com.liam.term.domain.term.vo.TermBatchDeleteVO;
import com.liam.term.service.term.ITermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/term")
public class TermController extends BaseController {

    @Autowired
    private ITermService termService;

    @GetMapping("/list")
    public TableDataInfo list(TermQueryDTO termQueryDTO) {
        return termService.list(termQueryDTO);
    }

    @PostMapping("/add")
    public R<Void> add(@RequestBody TermAddDTO termAddDTO) {
        return toR(termService.add(termAddDTO));
    }

    @PutMapping("/edit")
    public R<Void> edit(@RequestBody TermEditDTO termEditDTO) {
        return toR(termService.edit(termEditDTO));
    }

    @DeleteMapping("/delete")
    public R<Void> delete(@RequestParam Long termId) {
        return toR(termService.delete(termId));
    }

    @PostMapping("/add/batch")
    public R<TermBatchAddVO> batchAdd(TermBatchAddDTO termBatchAddDTO) {
        return R.ok(termService.batchAdd(termBatchAddDTO));
    }

    @DeleteMapping("/delete/batch")
    public R<TermBatchDeleteVO> batchDelete(@RequestBody TermBatchDeleteDTO termBatchDeleteDTO) {
        return R.ok(termService.batchDelete(termBatchDeleteDTO));
    }

    @GetMapping("/export")
    public TableDataInfo export(TermQueryDTO termQueryDTO) {
        // 导出时设置一个很大的pageSize来获取所有数据
        termQueryDTO.setPageSize(10000);
        return termService.list(termQueryDTO);
    }
}
