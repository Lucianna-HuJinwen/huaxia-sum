package com.liam.term.utils;

import cn.hutool.core.util.StrUtil;
import com.liam.common.core.constants.CacheConstants;
import com.liam.common.core.constants.Constants;
import com.liam.common.core.domain.LoginUser;
import com.liam.common.core.utils.ThreadLocalUtil;
import com.liam.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户角色工具类
 * 角色定义：0-管理员，1-普通用户
 */
@Slf4j
@Component
public class UserRoleUtils {

    @Autowired
    private RedisService redisService;

    /**
     * 管理员角色
     */
    public static final Integer ROLE_ADMIN = 0;

    /**
     * 普通用户角色
     */
    public static final Integer ROLE_USER = 1;

    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        return ThreadLocalUtil.get(Constants.USER_ID, Long.class);
    }

    /**
     * 获取当前用户角色
     * @return 用户角色：0-管理员，1-普通用户
     */
    public Integer getCurrentUserRole() {
        String userKey = ThreadLocalUtil.get(Constants.USER_KEY, String.class);
        if (StrUtil.isEmpty(userKey)) {
            log.warn("无法获取用户Key，默认返回普通用户角色");
            return ROLE_USER;
        }

        String tokenKey = CacheConstants.LOGIN_TOKEN_KEY + userKey;
        LoginUser loginUser = redisService.getCacheObject(tokenKey, LoginUser.class);
        if (loginUser != null && loginUser.getIdentity() != null) {
            // 这里根据LoginUser中的identity字段映射到role
            // identity: 1普通用户 2管理员 -> role: 1普通用户 0管理员
            if (loginUser.getIdentity() == 2) {
                return ROLE_ADMIN; // 管理员
            }
        }

        return ROLE_USER; // 默认普通用户
    }

    /**
     * 判断当前用户是否为管理员
     */
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(getCurrentUserRole());
    }

    /**
     * 判断当前用户是否为普通用户
     */
    public boolean isUser() {
        return ROLE_USER.equals(getCurrentUserRole());
    }

    /**
     * 检查用户是否有权限访问术语库
     * @param glossaryUserId 术语库所属用户ID
     * @param isPublic 是否公开：0-私有，1-公开
     * @return true-有权限，false-无权限
     */
    public boolean canAccessGlossary(Long glossaryUserId, Integer isPublic) {
        Long currentUserId = getCurrentUserId();
        Integer currentUserRole = getCurrentUserRole();

        if (currentUserId == null) {
            return false;
        }

        // 管理员可以访问所有术语库
        if (ROLE_ADMIN.equals(currentUserRole)) {
            return true;
        }

        // 公开术语库，所有用户都可以访问
        if (isPublic != null && isPublic == 1) {
            return true;
        }

        // 私有术语库，只有创建者可以访问
        if (isPublic != null && isPublic == 0) {
            return currentUserId.equals(glossaryUserId);
        }

        // 其他情况不允许访问
        return false;
    }

    /**
     * 检查用户是否有权限编辑术语库
     * @param glossaryUserId 术语库所属用户ID
     * @param isPublic 是否公开：0-私有，1-公开
     * @return true-有权限，false-无权限
     */
    public boolean canEditGlossary(Long glossaryUserId, Integer isPublic) {
        Long currentUserId = getCurrentUserId();
        Integer currentUserRole = getCurrentUserRole();

        if (currentUserId == null) {
            return false;
        }

        // 公开术语库只有管理员可以编辑
        if (isPublic != null && isPublic == 1) {
            return ROLE_ADMIN.equals(currentUserRole);
        }

        // 私有术语库只有创建者可以编辑
        if (isPublic != null && isPublic == 0) {
            return currentUserId.equals(glossaryUserId);
        }

        return false;
    }
} 