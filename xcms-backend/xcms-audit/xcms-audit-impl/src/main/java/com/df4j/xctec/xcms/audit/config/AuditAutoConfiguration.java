package com.df4j.xctec.xcms.audit.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 审计模块自动配置。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.audit")
@EntityScan(basePackages = "com.df4j.xctec.xcms.audit.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.audit.repository")
public class AuditAutoConfiguration {
}
