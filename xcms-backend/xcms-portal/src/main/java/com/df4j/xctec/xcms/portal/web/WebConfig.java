package com.df4j.xctec.xcms.portal.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Portal 全局 Web 配置：注册租户上下文拦截器。
 *
 * <p>拦截器对所有请求生效，但排除 actuator、OpenAPI 文档及免认证的登录/刷新端点。
 * 由于已移除 context-path，排除路径需使用控制器实际暴露的路径（含 {@code /api} 前缀）。
 * CORS 等纯全局基础设施配置保留在 app 模块。</p>
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
