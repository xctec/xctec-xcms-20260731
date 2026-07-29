package com.df4j.xctec.xcms.web.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAPI 文档信息配置项（AT-29），对应 springdoc 原生 {@code springdoc.info.*} 命名。
 *
 * <p>title/version 未配置时由 {@link WebStarterOpenApiAutoConfiguration} 回退到
 * {@code spring.application.name}。</p>
 */
@ConfigurationProperties(prefix = "springdoc.info")
public class XcmsSpringdocProperties {

    private String title;

    private String version;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
