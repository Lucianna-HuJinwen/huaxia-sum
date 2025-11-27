package com.liam.user.service.user.impl;

import com.liam.user.service.user.IEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-01-25
 * @Description: 邮件发送服务
 * @Version: 1.0
 */

@Service
@Slf4j
public class EmailServiceImpl implements IEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code    验证码
     * @return 发送结果
     */
    @Override
    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("华夏译典通 - 安全验证");
            
            // 使用HTML模板
            String htmlContent = generateVerificationCodeHtml(code);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人：{}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("验证码邮件发送失败，收件人：{}，错误信息：{}", toEmail, e.getMessage());
            return false;
        }
    }

    /**
     * 异步发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code    验证码
     */
    @Override
    @Async("emailTaskExecutor")
    public void sendVerificationCodeAsync(String toEmail, String code) {
        try {
            sendVerificationCode(toEmail, code);
            log.info("异步邮件发送完成，收件人：{}", toEmail);
        } catch (Exception e) {
            log.error("异步邮件发送异常，收件人：{}，错误信息：{}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * 生成验证码HTML邮件内容
     *
     * @param code 验证码
     * @return HTML内容
     */
    private String generateVerificationCodeHtml(String code) {
        try {
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
            
            // 读取HTML模板
            ClassPathResource resource = new ClassPathResource("templates/email/verification-code.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            // 替换模板中的占位符
            String html = template
                    .replace("{{CODE}}", code)
                    .replace("{{TIMESTAMP}}", currentTime);
            
            return html;
        } catch (IOException e) {
            log.error("读取邮件模板失败", e);
            // 如果模板读取失败，返回简单的HTML
            return generateSimpleHtml(code);
        }
    }
    
    /**
     * 生成简单的HTML邮件内容（备用方案）
     *
     * @param code 验证码
     * @return HTML内容
     */
    private String generateSimpleHtml(String code) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
        
        return "<!DOCTYPE html>" +
                "<html lang=\"zh-CN\">" +
                "<head><meta charset=\"UTF-8\"><title>华夏译典通</title></head>" +
                "<body style=\"font-family: Arial, sans-serif; background: #f5f5f5; padding: 20px;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);\">" +
                "<h1 style=\"color: #333; text-align: center; margin-bottom: 30px;\">华夏译典通</h1>" +
                "<div style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; text-align: center; margin: 20px 0;\">" +
                "<h2 style=\"margin-bottom: 20px;\">您的验证码</h2>" +
                "<div style=\"font-size: 36px; font-weight: bold; letter-spacing: 8px; margin: 20px 0;\">" + code + "</div>" +
                "<p>验证码有效期为 5 分钟</p>" +
                "</div>" +
                "<div style=\"background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0; color: #856404;\">" +
                "<p><strong>安全提醒：</strong></p>" +
                "<ul>" +
                "<li>请勿将验证码泄露给他人</li>" +
                "<li>如非本人操作，请忽略此邮件</li>" +
                "<li>验证码仅用于身份验证</li>" +
                "</ul>" +
                "</div>" +
                "<p style=\"color: #666; font-size: 14px;\"><strong>发送时间：</strong>" + currentTime + "</p>" +
                "<p style=\"color: #666; font-size: 14px;\"><strong>如果您没有请求此验证码，请忽略此邮件。</strong></p>" +
                "<div style=\"text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666;\">" +
                "<p><strong>华夏科技</strong></p>" +
                "<p>致力于为您提供安全、便捷的服务体验</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}