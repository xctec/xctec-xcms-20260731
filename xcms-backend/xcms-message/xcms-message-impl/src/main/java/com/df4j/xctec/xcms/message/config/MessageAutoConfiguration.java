package com.df4j.xctec.xcms.message.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 消息模块自动配置。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.message")
@EntityScan(basePackages = "com.df4j.xctec.xcms.message.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.message.repository")
public class MessageAutoConfiguration {
}
