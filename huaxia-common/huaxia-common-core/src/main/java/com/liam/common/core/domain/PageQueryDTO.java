package com.liam.common.core.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-04-04
 * @Description:
 * @Version: 1.0
 */

@Getter
@Setter
public class PageQueryDTO {

    private Integer pageSize = 10; // 每页数据数量

    private Integer pageNum = 1; // 第几页
}
