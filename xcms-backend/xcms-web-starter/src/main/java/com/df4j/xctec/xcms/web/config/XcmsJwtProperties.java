package com.df4j.xctec.xcms.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT Resource Server 配置项（AT-29）。
 *
 * <p>与 identity 模块签发端 {@code xcms.identity.jwt.secret} 同源（建议统一指向
 * 同一密钥/环境变量）。未显式配置时回退到与签发端一致的默认值。</p>
 */
@ConfigurationProperties(prefix = "xcms.jwt")
public class XcmsJwtProperties {

    private String secret = "xcms-jwt-default-secret-key-2026-07-25!";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
