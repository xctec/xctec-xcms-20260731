package com.df4j.xctec.xcms.web.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAPI 元信息配置（评审 P2-5）：将原本硬编码的标题/描述/联系人改为可配置，
 * 每个微服务可定制自己的文档信息；未配置时使用合理默认值。
 *
 * <p><b>命名说明（评审 P2-6）：</b>此属性前缀 {@code springdoc.info} 为 starter 自定义命名空间，
 * 并非 springdoc 原生命名（springdoc 原生没有 springdoc.info.title/version 这类属性，
 * 其文档元信息由 {@code springdoc.swagger-ui.*} 与 OpenAPI Bean 控制）。保留该前缀仅为
 * 与 springdoc 习惯对齐、降低迁移心智成本。title/version 缺省时回退到
 * {@code spring.application.name}。</p>
 */
@ConfigurationProperties(prefix = "springdoc.info")
public class XcmsSpringdocProperties {

    private String title;

    private String version;

    private String description = "XCMS 服务 API 文档（由 xcms-web-starter 自动装配）";

    private String contactName = "XCMS Team";

    private String contactEmail = "xcms@example.com";

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}
