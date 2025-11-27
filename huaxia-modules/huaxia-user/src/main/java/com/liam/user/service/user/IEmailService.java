package com.liam.user.service.user;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-07-18
 * @Description:
 * @Version: 1.0
 */

public interface IEmailService {

    /**
     * 同步发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code    验证码
     * @return 发送结果
     */
    boolean sendVerificationCode(String toEmail, String code);

    /**
     * 异步发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code    验证码
     */
    void sendVerificationCodeAsync(String toEmail, String code);
}
