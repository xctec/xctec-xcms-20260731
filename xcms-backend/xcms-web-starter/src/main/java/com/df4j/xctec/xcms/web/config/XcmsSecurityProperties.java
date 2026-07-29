package com.df4j.xctec.xcms.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Web 安全配置项（AT-29）。
 *
 * <p>免认证路径白名单：与单体 Portal {@code WebConfig} 的拦截器排除路径保持一致。
 * 未配置 {@code xcms.security.permit-paths} 时使用以下默认值。</p>
 */
@ConfigurationProperties(prefix = "xcms.security")
public class XcmsSecurityProperties {

    private List<String> permitPaths = new ArrayList<>(List.of(
            "/api/auth/login", "/api/auth/refresh",
            "/api/tenant/lookup",
            "/api/sso/authorize", "/api/sso/callback",
            "/actuator/**",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**",
            "/error", "/favicon.ico",
            "/h2-console/**"
    ));

    public List<String> getPermitPaths() {
        return permitPaths;
    }

    public void setPermitPaths(List<String> permitPaths) {
        this.permitPaths = permitPaths;
    }
}
