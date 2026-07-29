package com.df4j.xctec.xcms.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * web 层配置（评审 P1-2）：CORS 允许的源。
 *
 * <p>对应 application.yml 中的 {@code xcms.web.cors.allowed-origins}（List）。
 * 默认值 {@code ["*"]} 仅用于开发；生产应通过配置改为具体前端域名。</p>
 */
@ConfigurationProperties(prefix = "xcms.web")
public class XcmsWebProperties {

    private Cors cors = new Cors();

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public static class Cors {
        /** 允许跨域的源；含 "*" 时 starter 自动改用 allowedOriginPatterns（兼容 allowCredentials=true）。 */
        private List<String> allowedOrigins = new ArrayList<>(List.of("*"));

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }
}
