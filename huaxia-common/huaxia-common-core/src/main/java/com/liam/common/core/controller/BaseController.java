package com.liam.common.core.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageInfo;
import com.liam.common.core.domain.R;
import com.liam.common.core.domain.TableDataInfo;


import java.util.List;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-28
 * @Description: 公共方法提取类
 *
 * @Version: 1.0
 */

public class BaseController {

    public R<Void> toR(int rows) {
        return rows > 0 ? R.ok() : R.fail();
    }

    public R<Void> toR(boolean result) {
        return result ? R.ok() : R.fail();
    }

    public TableDataInfo getTableDataInfo(List<?> list) {
        if(CollectionUtil.isEmpty(list)) {
            return TableDataInfo.empty();
        }
//        new PageInfo<>(list).getTotal(); // 获取符合查询条件的数据的总数
//        return TableDataInfo.success(list, list.size()); // 前端 分页查询,不能直接用size()
        return TableDataInfo.success(list, new PageInfo(list).getTotal());
    }
}
