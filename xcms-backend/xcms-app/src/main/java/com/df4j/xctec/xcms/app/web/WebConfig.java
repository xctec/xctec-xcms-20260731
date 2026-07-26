package com.df4j.xctec.xcms.app.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * app 全局 Web 配置：仅负责 CORS（真正的跨模块基础设施）。
 * 租户上下文拦截器由 Portal 模块注册。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final List<String> allowedOrigins;

    public WebConfig(@Value("${xcms.web.cors.allowed-origins:*}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
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
