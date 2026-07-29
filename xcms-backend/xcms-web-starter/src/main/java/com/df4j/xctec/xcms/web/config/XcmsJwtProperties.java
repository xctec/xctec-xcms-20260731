package com.df4j.xctec.xcms.web.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 校验密钥配置（ADR-016）。
 *
 * <p>Resource Server 校验密钥，与 identity 模块签发端同源（默认均取 ${XCMS_JWT_SECRET}）。</p>
 *
 * <p><b>评审 P1-3：</b>内置默认密钥仅用于开发/演示。若未通过环境变量
 * {@code XCMS_JWT_SECRET} 显式覆盖，启动时输出 WARN，提示存在令牌被伪造的风险。</p>
 */
@Slf4j
@ConfigurationProperties(prefix = "xcms.jwt")
public class XcmsJwtProperties {

    /** 内置默认密钥（仅开发/演示用）。生产必须通过 XCMS_JWT_SECRET 环境变量覆盖。 */
    public static final String DEFAULT_SECRET = "xcms-jwt-default-secret-key-2026-07-25!";

    private String secret = DEFAULT_SECRET;

    @PostConstruct
    public void validate() {
        if (DEFAULT_SECRET.equals(secret)) {
            log.warn("xcms.jwt.secret 未显式配置，正在使用内置默认弱密钥（仅限开发/演示环境）。"
                    + " 生产环境必须通过环境变量 XCMS_JWT_SECRET 设置强密钥，否则存在 JWT 令牌被伪造的风险。");
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
