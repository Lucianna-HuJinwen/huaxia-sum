package com.liam.user.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import com.liam.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-04-16
 * @Description:
 * @Version: 1.0
 */

@Getter
@Setter
@TableName("user_account")
public class User extends BaseEntity {

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long userId;

    private String nickName;

    private String email;

    private String password;

    private Integer status; // 正常(1) 封禁(0)

    private Integer role; // 普通用户(1) 和管理员(0)
}
