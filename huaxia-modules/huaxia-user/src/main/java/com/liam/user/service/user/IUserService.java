package com.liam.user.service.user;

import com.liam.common.core.domain.R;
import com.liam.user.domain.user.dto.UserRegisterDTO;
import com.liam.user.domain.user.dto.UserUpdateDTO;
import com.liam.user.domain.user.vo.UserVO;
import com.liam.common.core.domain.vo.LoginUserVO;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-07-18
 * @Description:
 * @Version: 1.0
 */

public interface IUserService {

    boolean sendCode(String email);

    String codeLogin(String phone, String code);

    String pwdLogin(String email, String password);

    boolean register(UserRegisterDTO userRegisterDTO);

    boolean logout(String token);

    R<LoginUserVO> info(String token);

    UserVO detail();

    int edit(UserUpdateDTO userUpdateDTO);

}