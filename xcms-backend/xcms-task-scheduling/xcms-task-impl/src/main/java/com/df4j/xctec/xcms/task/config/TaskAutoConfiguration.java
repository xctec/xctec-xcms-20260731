package com.df4j.xctec.xcms.task.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 任务调度模块自动配置：扫描组件、实体与 JPA 仓库。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.task")
@EntityScan(basePackages = "com.df4j.xctec.xcms.task.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.task.repository")
public class TaskAutoConfiguration {
}
