package com.liam.common.core.enums;

import lombok.Getter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-26
 * @Description:
 * @Version: 1.0
 */

@Getter
public enum UserIdentity {

    NORMAL(1, "普通用户"),

    ADMIN(0, "管理员");

    private Integer value;

    private String des;

    UserIdentity(int value, String des) {
        this.value = value;
        this.des = des;
    }
}
