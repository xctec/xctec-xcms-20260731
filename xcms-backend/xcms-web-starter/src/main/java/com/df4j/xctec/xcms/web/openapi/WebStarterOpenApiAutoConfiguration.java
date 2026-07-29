package com.df4j.xctec.xcms.web.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * OpenAPI 文档自动配置（AT-29，由原 xcms-app 的 OpenApiConfig 迁入）。
 *
 * <p>title/version 通过 {@code springdoc.info.title}/{@code springdoc.info.version}
 * 配置，默认取 {@code spring.application.name}，便于各服务展示自身名称。</p>
 *
 * <p>使用 {@link AutoConfigureOrder#HIGHEST_PRECEDENCE} 确保本 {@code OpenAPI} Bean
 * 优先于 springdoc 的默认 Bean 注册，避免重复 Bean 冲突。</p>
 */
@AutoConfiguration
@ConditionalOnWebApplication
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(XcmsSpringdocProperties.class)
public class WebStarterOpenApiAutoConfiguration {

    public static final String SECURITY_SCHEME_BEARER = "Bearer";

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI xcmsOpenAPI(XcmsSpringdocProperties props,
                               @Value("${spring.application.name:xcms}") String appName) {
        String title = (props.getTitle() != null && !props.getTitle().isBlank())
                ? props.getTitle() : appName;
        String version = (props.getVersion() != null && !props.getVersion().isBlank())
                ? props.getVersion() : appName;
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description("XCMS 统一内容管理平台 API 文档（由 xcms-web-starter 自动装配）")
                        .version(version)
                        .contact(new Contact().name("XCMS Team").email("xcms@example.com"))
                        .license(new License().name("Proprietary").url("")))
                .servers(List.of(new Server().url("/").description("当前服务")))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_BEARER,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_BEARER));
    }
}
