package com.df4j.xctec.xcms.app.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 全局 Web 配置：注册租户上下文拦截器与 CORS。
 *
 * <p>拦截器对全部请求生效，但排除 actuator、OpenAPI 文档及免认证的登录/刷新端点。</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;
    private final List<String> allowedOrigins;

    public WebConfig(TenantInterceptor tenantInterceptor,
                     @Value("${xcms.web.cors.allowed-origins:*}") List<String> allowedOrigins) {
        this.tenantInterceptor = tenantInterceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/webjars/**",
                        "/error", "/favicon.ico",
                        "/auth/login", "/auth/refresh", "/login");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
