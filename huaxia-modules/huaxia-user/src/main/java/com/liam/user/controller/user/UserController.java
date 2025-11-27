package com.liam.user.controller.user;

import com.liam.common.core.controller.BaseController;
import com.liam.user.service.user.IUserService;
import com.liam.common.core.constants.HttpConstants;
import com.liam.common.core.domain.R;
import com.liam.user.domain.user.dto.UserLoginDTO;
import com.liam.user.domain.user.dto.UserRegisterDTO;
import com.liam.user.domain.user.dto.UserUpdateDTO;
import com.liam.user.domain.user.vo.UserVO;
import com.liam.common.core.domain.vo.LoginUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-07-18
 * @Description:
 * @Version: 1.0
 */

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private IUserService userService;

    // /user/sendCode
    @PostMapping("/sendCode")
    public R<Void> sendCode(@RequestParam String email) {
        return toR(userService.sendCode(email));
    }

    @PostMapping("/register")
    public R<Void> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        return toR(userService.register(userRegisterDTO));
    }

    @PostMapping("/pwd/login")
    public R<String> pwdLogin(@RequestBody UserLoginDTO userLoginDTO) {
        return R.ok(userService.pwdLogin(userLoginDTO.getEmail(), userLoginDTO.getPassword()));
    }

    @DeleteMapping("/logout")
    public R<Void> logout(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        return toR(userService.logout(token));
    }

    @GetMapping("/info")
    public R<LoginUserVO> info(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        return userService.info(token);
    }

    @GetMapping("/detail")
    public R<UserVO> detail() {
        return R.ok(userService.detail());
    }

    @PutMapping("/edit")
    public R<Void> edit(@RequestBody UserUpdateDTO userUpdateDTO) {
        return toR(userService.edit(userUpdateDTO));
    }
}
