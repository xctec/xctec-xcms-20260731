package com.df4j.xctec.xcms.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 全局 OpenAPI 文档配置。
 *
 * <p>定义 API 元信息（标题/版本/描述）、统一 Bearer JWT 安全方案。所有受保护端点默认要求
 * Bearer 令牌；公开端点（登录、刷新、SSO 回调等）在对应 Controller 上以
 * {@code @SecurityRequirements} 显式豁免，避免被标记为需要鉴权。</p>
 */
@Configuration
public class OpenApiConfig {

    /** 安全方案名称（Bearer JWT）。 */
    public static final String SECURITY_SCHEME_BEARER = "Bearer";

    @Bean
    public OpenAPI xcmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("XCMS 统一内容管理平台 API")
                        .description("""
                                XC-TEC 企业级统一内容管理平台（XCMS）后端 OpenAPI 文档。

                                ## 鉴权
                                除登录（/api/auth/login）、刷新（/api/auth/refresh）、SSO 回调等公开端点外，
                                所有请求需在 HTTP Header 携带 `Authorization: Bearer <token>`，
                                令牌由 /api/auth/login 登录后返回。

                                ## 多租户
                                租户由 JWT 解析（也可通过 X-Tenant 头切换），所有业务数据按 tenant_id 行级隔离。
                                缺少有效凭证或令牌无效/过期时返回 401。

                                ## 统一响应
                                成功响应包裹于 ApiResponse<T>（data 字段为业务对象，code=000000 表示成功）；
                                业务异常返回对应 code 与 message。""")
                        .version("v1.0.0")
                        .contact(new Contact().name("XC-TEC").email("dev@xctec.com"))
                        .license(new License().name("Proprietary")))
                .servers(List.of(new Server().url("/").description("当前运行实例")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_BEARER,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Bearer 令牌，由 /api/auth/login 获取")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_BEARER));
    }
}
