package com.liam.translate.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 翻译统计VO
 * 
 * @Author: LiamLMK
 * @CreateTime: 2025-01-XX
 * @Description: 翻译统计展示对象
 * @Version: 1.0
 */
@Data
public class TranslateStatisticsVO {

    /**
     * 统计记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 用户名（从用户表关联获取）
     */
    private String username;

    /**
     * 选中的术语库数量
     */
    private Integer selectedGlossaryCount;

    /**
     * 匹配到的术语数量
     */
    private Integer matchedTermCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
