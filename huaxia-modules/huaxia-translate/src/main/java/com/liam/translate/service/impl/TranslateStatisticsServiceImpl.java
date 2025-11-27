package com.liam.translate.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liam.translate.domain.entity.TranslateStatistics;
import com.liam.translate.domain.vo.TranslateStatisticsVO;
import com.liam.translate.mapper.TranslateStatisticsMapper;
import com.liam.translate.service.ITranslateStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 翻译统计服务实现类
 * 
 * @Author: LiamLMK
 * @CreateTime: 2025-01-XX
 * @Description: 翻译统计相关业务逻辑实现
 * @Version: 1.0
 */
@Slf4j
@Service
public class TranslateStatisticsServiceImpl implements ITranslateStatisticsService {

    @Autowired
    private TranslateStatisticsMapper translateStatisticsMapper;

    @Override
    public void recordTranslationStatistics(Long userId, Integer selectedGlossaryCount, Integer matchedTermCount) {
        try {
            TranslateStatistics statistics = new TranslateStatistics();
            statistics.setUserId(userId);
            statistics.setSelectedGlossaryCount(selectedGlossaryCount != null ? selectedGlossaryCount : 0);
            statistics.setMatchedTermCount(matchedTermCount != null ? matchedTermCount : 0);
            
            translateStatisticsMapper.insert(statistics);
            
            log.info("翻译统计记录成功: 用户ID={}, 术语库数量={}, 匹配术语数量={}", 
                userId, selectedGlossaryCount, matchedTermCount);
                
        } catch (Exception e) {
            log.error("记录翻译统计失败: 用户ID={}, 术语库数量={}, 匹配术语数量={}, 错误={}", 
                userId, selectedGlossaryCount, matchedTermCount, e.getMessage(), e);
        }
    }

    @Override
    public Page<TranslateStatisticsVO> getTranslateStatisticsPage(Integer pageNum, Integer pageSize, Long userId) {
        try {
            Page<TranslateStatisticsVO> page = new Page<>(pageNum, pageSize);
            return translateStatisticsMapper.selectTranslateStatisticsPage(page, userId);
        } catch (Exception e) {
            log.error("查询翻译统计失败: pageNum={}, pageSize={}, userId={}, 错误={}", 
                pageNum, pageSize, userId, e.getMessage(), e);
            return new Page<>();
        }
    }
}
