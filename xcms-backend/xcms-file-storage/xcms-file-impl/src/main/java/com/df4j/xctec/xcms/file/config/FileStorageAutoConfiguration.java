package com.df4j.xctec.xcms.file.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 文件存储模块自动配置。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.file")
@EntityScan(basePackages = "com.df4j.xctec.xcms.file.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.file.repository")
public class FileStorageAutoConfiguration {
}
