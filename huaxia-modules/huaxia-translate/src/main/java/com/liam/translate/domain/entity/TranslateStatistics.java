package com.liam.translate.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.liam.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 翻译统计实体类
 * 
 * @Author: LiamLMK
 * @CreateTime: 2025-01-XX
 * @Description: 记录用户翻译时的术语库使用统计
 * @Version: 1.0
 */
@Getter
@Setter
@TableName("translate_statistics")
public class TranslateStatistics extends BaseEntity {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 选中的术语库数量
     */
    private Integer selectedGlossaryCount;

    /**
     * 匹配到的术语数量
     */
    private Integer matchedTermCount;
}
