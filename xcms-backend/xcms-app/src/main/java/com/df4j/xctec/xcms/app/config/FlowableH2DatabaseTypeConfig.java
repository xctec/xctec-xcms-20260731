package com.df4j.xctec.xcms.app.config;

import org.flowable.app.spring.SpringAppEngineConfiguration;
import org.flowable.cmmn.spring.SpringCmmnEngineConfiguration;
import org.flowable.dmn.spring.SpringDmnEngineConfiguration;
import org.flowable.eventregistry.spring.SpringEventRegistryEngineConfiguration;
import org.flowable.idm.spring.SpringIdmEngineConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * h2 开发 profile 下强制 Flowable 各引擎使用 MySQL 数据库类型。
 *
 * <p>Flowable 8.0.0 的 H2 建表脚本仍使用 {@code IDENTITY} 数据类型，与 Spring Boot 4 自带的
 * H2 2.x 不兼容（"Unknown data type: IDENTITY"，已验证 flowable 8 未修复此兼容）。开发模式
 * H2 已启用 {@code MODE=MySQL}，强制 Flowable 使用 MySQL 建表脚本（{@code AUTO_INCREMENT}）
 * 即可兼容。生产 mysql profile 由 Flowable 自动检测为 MySQL，无需此配置。</p>
 */
@Configuration
@Profile("h2")
public class FlowableH2DatabaseTypeConfig {

    private static final String MYSQL = "mysql";

    @Bean
    public EngineConfigurationConfigurer<SpringAppEngineConfiguration> appEngineDbType() {
        return c -> c.setDatabaseType(MYSQL);
    }

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processEngineDbType() {
        return c -> c.setDatabaseType(MYSQL);
    }

    @Bean
    public EngineConfigurationConfigurer<SpringCmmnEngineConfiguration> cmmnEngineDbType() {
        return c -> c.setDatabaseType(MYSQL);
    }

    @Bean
    public EngineConfigurationConfigurer<SpringDmnEngineConfiguration> dmnEngineDbType() {
        return c -> c.setDatabaseType(MYSQL);
    }

    @Bean
    public EngineConfigurationConfigurer<SpringEventRegistryEngineConfiguration> eventRegistryEngineDbType() {
        return c -> c.setDatabaseType(MYSQL);
    }

    @Bean
    public EngineConfigurationConfigurer<SpringIdmEngineConfiguration> idmEngineDbType() {
        return c -> c.setDatabaseType(MYSQL);
    }
}
