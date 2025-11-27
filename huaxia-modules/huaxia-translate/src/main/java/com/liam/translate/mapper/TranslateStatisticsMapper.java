package com.liam.translate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liam.translate.domain.entity.TranslateStatistics;
import com.liam.translate.domain.vo.TranslateStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 翻译统计Mapper接口
 * 
 * @Author: LiamLMK
 * @CreateTime: 2025-01-XX
 * @Description: 翻译统计数据访问层
 * @Version: 1.0
 */
@Mapper
public interface TranslateStatisticsMapper extends BaseMapper<TranslateStatistics> {

    /**
     * 分页查询翻译统计数据（包含用户名）
     * 
     * @param page 分页对象
     * @param userId 用户ID（可选）
     * @return 分页结果
     */
    Page<TranslateStatisticsVO> selectTranslateStatisticsPage(Page<TranslateStatisticsVO> page, @Param("userId") Long userId);
}
