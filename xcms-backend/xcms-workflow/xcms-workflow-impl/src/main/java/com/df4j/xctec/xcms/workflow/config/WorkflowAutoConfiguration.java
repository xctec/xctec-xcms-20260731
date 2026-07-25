package com.df4j.xctec.xcms.workflow.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 工作流模块自动配置（最上层模块，整合 Flowable 与 identity/authorization/message/file-storage/configuration）。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.workflow")
@EntityScan(basePackages = "com.df4j.xctec.xcms.workflow.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.workflow.repository")
public class WorkflowAutoConfiguration {
}
