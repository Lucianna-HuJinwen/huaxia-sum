package com.liam.user.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-05-04
 * @Description:
 * @Version: 1.0
 */


@Getter
@Setter
public class UserSubmitDTO {

    private Long examId;  //可选

    private Long questionId;

    private Integer programType;  // (0: java  1:cpp 2: golang)

    private String userCode;
}