package com.liam.common.core.constants;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-26
 * @Description: 缓存常量
 * @Version: 1.0
 */

public class CacheConstants {

    public final static String LOGIN_TOKEN_KEY = "logintoken:";

    public final static long EXPIRATION = 720;

    public static final long REFRESH_TIME = 3;

    public final static String EMAIL_CODE_KEY = "e:c:";

    public final static String CODE_TIME_KEY = "c:t:";

    // === Glossary（术语集）相关缓存 ===
    public final static String GLOSSARY_UNFINISHED_LIST = "g:u:l"; // 未发布术语集列表
    public final static String GLOSSARY_HISTORY_LIST = "g:h:l";    // 已发布术语集历史列表（如有需要）
    public final static String GLOSSARY_DETAIL = "g:d:";           // 术语集详情
    public final static String GLOSSARY_TERM_LIST = "g:t:l:";      // 某术语集下的术语列表

    // === User 相关 ===
    public final static String USER_GLOSSARY_LIST = "u:g:l:";      // 用户创建的术语集列表（如需要）
    public final static String USER_DETAIL = "u:d:";               // 用户详情信息
    public final static long USER_EXP = 10;
    public static final String USER_UPLOAD_TIMES_KEY = "u:u:t";

    // === Term / 题库类 ===
    public static final String TERM_LIST = "t:l";                  // 所有术语列表
    public static final String TERM_HOST_LIST = "t:h:l";           // 热门术语列表（如需）

    // === 消息类 ===
    public static final String USER_MESSAGE_LIST = "u:m:l:";
    public static final String MESSAGE_DETAIL = "m:d:";

    // === Template（模板）相关缓存 ===
    public final static String TEMPLATE_CACHE_KEY = "template:";
    public final static long TEMPLATE_CACHE_TTL = 3600; // 1小时

    // === 其他 ===
    public static final long DEFAULT_START = 0;
    public static final long DEFAULT_END = -1;
}

