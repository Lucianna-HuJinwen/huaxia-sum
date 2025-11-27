package com.liam.user.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-01-25
 * @Description: 用户注册DTO
 * @Version: 1.0
 */

@Getter
@Setter
public class UserRegisterDTO {

    private String email;

    private String password;

    private String confirmPassword;

    private String code;

    private String nickName;
}