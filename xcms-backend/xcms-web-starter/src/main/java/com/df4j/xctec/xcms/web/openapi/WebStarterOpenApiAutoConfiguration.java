package com.df4j.xctec.xcms.web.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * OpenAPI 文档自动配置（评审 P2-7：不再使用 @AutoConfigureOrder(HIGHEST_PRECEDENCE) 抢注）。
 *
 * <p>springdoc 2.x 本身并不注册类型为 {@link OpenAPI} 的 Spring Bean（其文档模型由
 * OpenAPIService 按需惰性构建），因此无需通过最高优先级与之竞争。此处仅以
 * {@link ConditionalOnMissingBean} 作为防御，允许消费方用自己的 OpenAPI Bean 覆盖。</p>
 *
 * <p>title/version 缺省回退到 {@code spring.application.name}；描述/联系人经
 * {@link XcmsSpringdocProperties} 配置（评审 P2-5）。</p>
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(XcmsSpringdocProperties.class)
public class WebStarterOpenApiAutoConfiguration {

    public static final String SECURITY_SCHEME_BEARER = "Bearer";

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI xcmsOpenAPI(XcmsSpringdocProperties props,
                               @Value("${spring.application.name:xcms}") String appName) {
        String title = (props.getTitle() != null && !props.getTitle().isBlank()) ? props.getTitle() : appName;
        String version = (props.getVersion() != null && !props.getVersion().isBlank()) ? props.getVersion() : appName;

        Info info = new Info()
                .title(title)
                .description(props.getDescription())
                .version(version)
                .contact(new Contact().name(props.getContactName()).email(props.getContactEmail()))
                .license(new License().name("Proprietary").url(""));

        return new OpenAPI()
                .info(info)
                .servers(List.of(new Server().url("/").description("当前服务")))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_BEARER));
    }
}
