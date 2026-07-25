package com.df4j.xctec.xcms.config.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 配置中心模块自动配置。模块化场景下由 Spring Boot 通过 AutoConfiguration.imports 自动装配。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.config")
@EntityScan(basePackages = "com.df4j.xctec.xcms.config.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.config.repository")
public class ConfigAutoConfiguration {
}
