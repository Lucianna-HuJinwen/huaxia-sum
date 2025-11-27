package com.liam.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.liam.common.core.constants.CacheConstants;
import com.liam.common.core.constants.Constants;
import com.liam.common.core.constants.HttpConstants;
import com.liam.common.core.domain.LoginUser;
import com.liam.common.core.domain.R;
import com.liam.common.core.enums.ResultCode;
import com.liam.common.core.enums.UserIdentity;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import com.liam.gateway.properties.IgnoreWhiteProperties;
import reactor.core.publisher.Mono;
import com.liam.redis.service.RedisService;
import com.liam.common.core.utils.JwtUtils;
import com.liam.common.core.utils.ThreadLocalUtil;

import java.util.List;
import com.alibaba.fastjson2.JSON;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-26
 * @Description: 网关鉴权
 * @Version: 1.0
 */

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    // 排除过滤的 uri 白名单地址，在nacos自行添加
    @Autowired
    private IgnoreWhiteProperties ignoreWhite;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private RedisService redisService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath(); // 请求的接口地址
        // 跳过不需要验证的路径 接口白名单：所有接口均不需要身份认证
        if (matches(url, ignoreWhite.getWhites())) { // 判断如果当前接口在白名单中，则不需要身份认证 return ignoreWhite.getWhites()：拿到nacos配置的接口地址的白名单
            return chain.filter(exchange);
        }

        // 此处 说明接口不在白名单中 需要身份认证
        // 从http请求头中获取token
        String token = getToken(request);
        if (StrUtil.isEmpty(token)) { // 没有携带token
            return unauthorizedResponse(exchange, "令牌不能为空");
        }
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息 解析payload中信息
            // claims存储着用户唯一表示信息
            if (claims == null) {
                return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
            }
        } catch (Exception e) {
            return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
        }

//        String userId = JwtUtils.getUserId(claims);
//        boolean isLogin = redisService.hasKey(getTokenKey(userId));

        // 通过redis中存储的数据，来控制jwt的过期时间
        String userKey = JwtUtils.getUserKey(claims); //获取jwt中的key
        boolean isLogin = redisService.hasKey(getTokenKey(userKey));
        if (!isLogin) {
            return unauthorizedResponse(exchange, "登录状态已过期");
        }
        String userid = JwtUtils.getUserId(claims); //判断jwt中的信息是否完整
        if (StrUtil.isEmpty(userid)) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }

        // 走到此处 token正确且没有过期
        // 判断redis存储 关于用户身份认证的信息是否正确
        // 判断当前请求 是C端功能 还是B端功能
        LoginUser user = redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);

        if (url.contains(HttpConstants.ADMIN_URL_PREFIX) &&
                !UserIdentity.ADMIN.getValue().equals(user.getIdentity())) {
            return unauthorizedResponse(exchange, "管理员令牌验证失败");
        }
        if (url.contains(HttpConstants.ADMIN_URL_PREFIX) &&
                !UserIdentity.NORMAL.getValue().equals(user.getIdentity())) {
            return unauthorizedResponse(exchange, "用户令牌验证失败");
        }

        ThreadLocalUtil.set(Constants.USER_ID, userid);

        return chain.filter(exchange);
    }

    /**
     * 查找指定url是否匹配指定匹配规则链表中的任意⼀个字符串
     *
     * @param url         指定url
     * @param patternList 需要检查的匹配规则链表
     * @return 是否匹配
     */
    private boolean matches(String url, List<String> patternList) {
        if (StrUtil.isEmpty(url) || CollectionUtils.isEmpty(patternList)) {
            return false;
        }
        // 接口地址如果和白名单其中一个地址匹配 返回true
        // 如果遍历完都没匹配到 返回false
        for (String pattern : patternList) {
            if (isMatch(pattern, url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断url是否与规则匹配
     * 匹配规则中：
     * pattern 可以写一些特殊字符：
     * ? 表⽰单个字符;
     * * 表⽰⼀层路径内的任意字符串，不可跨层级;
     * ** 表⽰任意层路径;
     *
     * @param pattern 匹配规则
     * @param url     需要匹配的url
     * @return 是否匹配
     */
    private boolean isMatch(String pattern, String url) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    /**
     * 获取缓存key
     */
    private String getTokenKey(String token) {
        return CacheConstants.LOGIN_TOKEN_KEY + token;
    }

    /**
     * 从请求头中获取请求token
     */
    private String getToken(ServerHttpRequest request) {
        String token =
                request.getHeaders().getFirst(HttpConstants.AUTHENTICATION);
// 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return token;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg) {
        log.error("[鉴权异常处理]请求路径:{}", exchange.getRequest().getPath());
        return webFluxResponseWriter(exchange.getResponse(), msg,
                ResultCode.FAILED_UNAUTHORIZED.getCode());
    }

    //拼装webflux模型响应
    private Mono<Void> webFluxResponseWriter(ServerHttpResponse response,
                                             String msg, int code) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_VALUE);
        R<?> result = R.fail(code, msg);
        DataBuffer dataBuffer =
                response.bufferFactory().wrap(JSON.toJSONString(result).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }

    @Override
    public int getOrder() {
        return -200; // 值越小 过滤器越先被执行
    }

}