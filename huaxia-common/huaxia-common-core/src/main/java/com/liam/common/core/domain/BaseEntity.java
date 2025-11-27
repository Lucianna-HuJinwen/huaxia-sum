package com.liam.common.core.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-20
 * @Description: 公共实体类
 * @Version: 1.0
 */

@Getter
@Setter
public class BaseEntity {

    private static final long serialVersionUID = 1L;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
