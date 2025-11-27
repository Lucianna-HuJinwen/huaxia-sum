package com.liam.user.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-04-23
 * @Description:
 * @Version: 1.0
 */

@Getter
@Setter
public class UserDTO {

    private String email;

    // 此处不应有code，因为系统还没发送
    private String code;

    private String password;
}
