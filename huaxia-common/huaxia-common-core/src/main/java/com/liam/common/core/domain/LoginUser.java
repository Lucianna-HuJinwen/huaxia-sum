package com.liam.common.core.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-26
 * @Description:
 * @Version: 1.0
 */

@Getter
@Setter
public class LoginUser {

    private Long userId; // 用户ID

    private String nickName; // 用户昵称

    private Integer identity;// identity: 0管理员 1普通用户

    private String email;
}
