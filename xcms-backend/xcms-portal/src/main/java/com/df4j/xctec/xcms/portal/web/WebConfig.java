package com.df4j.xctec.xcms.portal.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Portal 全局 Web 配置：注册租户上下文填充拦截器。
 *
 * <p>认证/授权已上收 Spring Security（AT-06/07），拦截器仅负责 TenantContext
 * 填充与清理；排除路径为免认证白名单（与 SecurityConfig PERMIT_ALL 一致），
 * 这些路径无认证信息、无需填充。CORS 等纯全局基础设施配置保留在 app 模块。</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    public WebConfig(TenantInterceptor tenantInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**",
                        "/error", "/favicon.ico",
                        "/api/auth/login", "/api/auth/refresh",
                        "/api/tenant/lookup",
                        "/api/sso/authorize", "/api/sso/callback",
                        "/h2-console/**");
    }
}
