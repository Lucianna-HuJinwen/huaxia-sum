package com.liam.common.core.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-04-01
 * @Description: 返回给前端的用户数据
 * @Version: 1.0
 */

@Getter
@Setter
public class LoginUserVO {

    private String nickName;

    private String email;
}
