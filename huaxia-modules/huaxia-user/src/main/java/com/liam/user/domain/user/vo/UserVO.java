package com.liam.user.domain.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-05-01
 * @Description:
 * @Version: 1.0
 */

@Getter
@Setter
public class UserVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String userName;

    private String email;

    private Integer status;

    private Integer role;
}