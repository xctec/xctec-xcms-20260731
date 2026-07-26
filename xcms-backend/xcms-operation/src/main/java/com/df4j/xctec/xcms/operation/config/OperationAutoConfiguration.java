package com.df4j.xctec.xcms.operation.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 运维中心模块自动配置。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.operation")
@EntityScan(basePackages = "com.df4j.xctec.xcms.operation.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.operation.repository")
public class OperationAutoConfiguration {
}
