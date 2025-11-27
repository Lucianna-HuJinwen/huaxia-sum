package com.liam.translate.service;

import com.liam.translate.domain.vo.TranslateStatisticsVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 翻译统计服务接口
 * 
 * @Author: LiamLMK
 * @CreateTime: 2025-01-XX
 * @Description: 翻译统计相关业务逻辑
 * @Version: 1.0
 */
public interface ITranslateStatisticsService {

    /**
     * 记录翻译统计信息
     * 
     * @param userId 用户ID
     * @param selectedGlossaryCount 选中的术语库数量
     * @param matchedTermCount 匹配到的术语数量
     */
    void recordTranslationStatistics(Long userId, Integer selectedGlossaryCount, Integer matchedTermCount);

    /**
     * 分页查询翻译统计数据
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID（可选，用于筛选）
     * @return 分页结果
     */
    Page<TranslateStatisticsVO> getTranslateStatisticsPage(Integer pageNum, Integer pageSize, Long userId);
}
