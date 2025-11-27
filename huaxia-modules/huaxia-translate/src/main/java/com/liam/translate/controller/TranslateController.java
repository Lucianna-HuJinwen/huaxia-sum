package com.liam.translate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liam.common.core.domain.R;
import com.liam.translate.domain.dto.CustomTranslateDTO;
import com.liam.translate.domain.dto.CreateAutomatonDTO;
import com.liam.translate.domain.vo.TranslateStatisticsVO;
import com.liam.translate.service.ITranslateService;
import com.liam.translate.service.ITranslateStatisticsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/translate")
public class TranslateController {

    @Autowired
    private ITranslateService translateService;

    @Autowired
    private ITranslateStatisticsService translateStatisticsService;

        /**
     * 创建自动机
     *
     * @param createAutomatonDTO 创建自动机的数据传输对象，包含术语表ID列表等信息
     * @return 返回包含会话ID的响应结果
     */
    @PostMapping("/automaton/create")
    public R<String> createAutomaton(@Valid @RequestBody CreateAutomatonDTO createAutomatonDTO){
        // 调用翻译服务创建自动机，获取会话ID
        String sessionId = translateService.createAutomaton(createAutomatonDTO.getGlossaryIdList());
        return R.ok(sessionId);
    }

    @PostMapping(value = "/custom/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> customFlux(@Valid @RequestBody CustomTranslateDTO customTranslateDTO){
        return translateService.customTranslateFlux(customTranslateDTO);
    }

    @PostMapping("/custom")
    public String custom(@Valid @RequestBody CustomTranslateDTO customTranslateDTO){
        return translateService.customTranslate(customTranslateDTO);
    }

    /**
     * 分页查询翻译统计数据
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID（可选，用于筛选）
     * @return 分页结果
     */
    @GetMapping("/statistics")
    public R<Page<TranslateStatisticsVO>> getTranslateStatistics(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long userId) {
        Page<TranslateStatisticsVO> result = translateStatisticsService.getTranslateStatisticsPage(pageNum, pageSize, userId);
        return R.ok(result);
    }
}
