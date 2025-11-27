package com.liam.user.service.user.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.liam.user.manager.user.UserCacheManager;
import com.liam.redis.service.RedisService;
import com.liam.user.service.TokenService;
import com.liam.user.service.user.IEmailService;
import com.liam.user.service.user.IUserService;
import com.liam.common.core.constants.CacheConstants;
import com.liam.common.core.constants.Constants;
import com.liam.common.core.constants.HttpConstants;
import com.liam.common.core.domain.LoginUser;
import com.liam.common.core.domain.R;
import com.liam.user.domain.user.User;
import com.liam.user.domain.user.dto.UserRegisterDTO;
import com.liam.user.domain.user.dto.UserUpdateDTO;
import com.liam.user.domain.user.vo.UserVO;
import com.liam.common.core.domain.vo.LoginUserVO;
import com.liam.common.core.enums.ResultCode;
import com.liam.common.core.enums.UserIdentity;
import exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import com.liam.user.domain.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.liam.common.core.utils.ThreadLocalUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-04-23
 * @Description:
 * @Version: 1.0
 */

@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserCacheManager userCacheManager;

    @Autowired
    private IEmailService emailService;

    @Value("${sms.code-expiration:5}")
    private Long emailCodeExpiration;

    @Value("${sms.send-limit:3}")
    private Integer sendLimit;

    @Value("${sms.is-send:false}")
    private boolean isSend; // 开关

    @Value("${jwt.secret}")
    private String secret;

    /**
     * 因运营商限制，由手机号验证改为邮箱验证
     *
     * @param email
     * @return
     */
    @Override
    public boolean sendCode(String email) {
        if (!checkEmail(email)) {
            throw new ServiceException(ResultCode.FAILED_USER_EMAIL);
        }
        //获取该邮箱对应的验证码Redis键
        //检查当前验证码的剩余有效期
        String emailCodeKey = getEmailCodeKey(email);
        Long expire = redisService.getExpire(emailCodeKey, TimeUnit.SECONDS);
        //如果验证码发送时间不足1分钟，则拒绝发送（防止频繁请求）
        if (expire != null && (emailCodeExpiration * 60 - expire) < 60) {
            throw new ServiceException(ResultCode.FAILED_FREQUENT);
        }
        // 每天的验证码获取次数有限制-50次 第二天计数清零 重新开始计数 计数 怎么存 存在哪
        // 操作次数数据频繁 不需要持久存储 有效时间 redis String key: c:t:手机号 value:
        // 获取已经请求的次数和50 进行比较 如果大于限制抛出异常。如果不大于限制，正常执行后续逻辑，并且将获取计数+1
        String codeTimeKey = getCodeTimeKey(email);
        Long sendTimes = redisService.getCacheObject(codeTimeKey, Long.class);
        if (sendTimes != null && sendTimes >= sendLimit) {
            throw new ServiceException(ResultCode.FAILED_TIME_LIMIT);
        }

        String code = isSend ? RandomUtil.randomNumbers(6) : Constants.DEFAULT_CODE;
        // 存储到redis中 数据结构: String key: p:c:手机号 value: code
        redisService.setCacheObject(getEmailCodeKey(email), code, emailCodeExpiration, TimeUnit.MINUTES);
        if (isSend) {
            // 异步发送邮件验证码
            sendEmailCodeAsync(email, code);
            // 异步发送不阻塞，直接返回成功
            log.info("验证码邮件已异步发送，收件人：{}", email);
        }
        redisService.increment(codeTimeKey);
        if (sendTimes == null) { // 说明是当天第一次发起获取验证码的请求
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            redisService.expire(codeTimeKey, seconds, TimeUnit.SECONDS);
        }
        return true;
    }

    // 添加邮件验证码发送方法（异步）
    private void sendEmailCodeAsync(String email, String code) {
        // 异步发送邮件，不阻塞用户操作
        emailService.sendVerificationCodeAsync(email, code);
    }

    @Override
    public String codeLogin(String email, String code) {
        // 先比对验证码
        checkCode(email, code);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) { // 新用户

        }
        return tokenService.createToken(user.getUserId(), secret, UserIdentity.NORMAL.getValue(), user.getNickName());
    }

    @Override
    public String pwdLogin(String email, String password) {
        if (StrUtil.isEmpty(email) || StrUtil.isEmpty(password)) {
            throw new ServiceException(ResultCode.FAILED_PARAMETER_VALIDATION);
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }

        // 验证密码是否正确
        if (!password.equals(user.getPassword())) {
            throw new ServiceException(ResultCode.FAILED_PASSWORD_ERROR);
        }

        return tokenService.createToken(user.getUserId(), secret, UserIdentity.NORMAL.getValue(), user.getNickName());
    }

    @Override
    public boolean register(UserRegisterDTO userRegisterDTO) {
        // 参数校验
        if (StrUtil.isEmpty(userRegisterDTO.getEmail()) ||
                StrUtil.isEmpty(userRegisterDTO.getPassword()) ||
                StrUtil.isEmpty(userRegisterDTO.getConfirmPassword()) ||
                StrUtil.isEmpty(userRegisterDTO.getCode()) ||
                StrUtil.isEmpty(userRegisterDTO.getNickName())) {
            throw new ServiceException(ResultCode.FAILED_PARAMETER_VALIDATION);
        }

        // 邮箱格式校验
        if (!checkEmail(userRegisterDTO.getEmail())) {
            throw new ServiceException(ResultCode.FAILED_USER_EMAIL);
        }

        // 密码确认校验
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            throw new ServiceException(ResultCode.FAILED_PARAMETER_VALIDATION);
        }

        // 验证码校验
        checkCode(userRegisterDTO.getEmail(), userRegisterDTO.getCode());

        // 检查邮箱是否已注册
        User existUser = userMapper
                .selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, userRegisterDTO.getEmail()));
        if (existUser != null) {
            throw new ServiceException(ResultCode.FAILED_USER_EXISTS);
        }

        // 创建新用户
        User user = new User();
        user.setEmail(userRegisterDTO.getEmail());
        user.setPassword(userRegisterDTO.getPassword()); // 实际项目中应该加密密码
        user.setNickName(userRegisterDTO.getNickName());
        user.setStatus(1); // 正常状态
        user.setRole(1); // 普通用户

        return userMapper.insert(user) > 0;
    }

    private void checkCode(String email, String code) {
        String emailCodeKey = getEmailCodeKey(email);
        String cacheCode = redisService.getCacheObject(emailCodeKey, String.class);
        if (StrUtil.isEmpty(cacheCode)) {
            throw new ServiceException(ResultCode.FAILED_INVALID_CODE);
        }
        if (!cacheCode.equals(code)) {
            throw new ServiceException(ResultCode.FAILED_ERROR_CODE);
        }
        // 验证码对比成功
        redisService.deleteObject(emailCodeKey);
    }

    @Override
    public boolean logout(String token) {
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return tokenService.deleteLoginUser(token, secret);
    }

    @Override
    public R<LoginUserVO> info(String token) {
        // 清除 Bearer 前缀
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        LoginUser loginUser = tokenService.getLoginUser(token, secret);
        if (loginUser == null) {
            return R.fail();
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setNickName(loginUser.getNickName());
        return R.ok(loginUserVO);
    }

    @Override
    public UserVO detail() {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if (userId == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        UserVO userVO = userCacheManager.getUserById(userId);
        if (userVO == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if (StrUtil.isNotEmpty(userVO.getEmail())) {
            userVO.setEmail(userVO.getEmail());
        }
        return userVO;
    }

    @Override
    public int edit(UserUpdateDTO userUpdateDTO) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        if (userId == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setNickName(userUpdateDTO.getNickName());
        user.setEmail(userUpdateDTO.getEmail());
        // 更新用户缓存
        userCacheManager.refreshUser(user);
        tokenService.refreshLoginUser(user.getNickName(), user.getEmail(),
                ThreadLocalUtil.get(Constants.USER_KEY, String.class));
        return userMapper.updateById(user);
    }

    private String getEmailCodeKey(String email) {
        return CacheConstants.EMAIL_CODE_KEY + email;
    }

    private String getCodeTimeKey(String email) {
        return CacheConstants.CODE_TIME_KEY + email;
    }

    public static boolean checkEmail(String email) {
        // 简单的邮箱格式验证
        Pattern regex = Pattern.compile("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$");
        Matcher m = regex.matcher(email);
        return m.matches();
    }
}
