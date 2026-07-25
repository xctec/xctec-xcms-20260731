package com.df4j.xctec.xcms.org.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 组织管理模块自动配置
 * <p>部署模块引入本模块后，由 Spring Boot 通过 META-INF/spring/AutoConfiguration.imports 自动装配以下 Bean：
 * 服务实现、JPA 仓储、实体扫描、MapStruct 映射器，以及监听租户创建事件初始化根部门的 TenantOrgInitializer。
 * 可通过 spring.autoconfigure.exclude 关闭本模块。</p>
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.org")
@EntityScan(basePackages = "com.df4j.xctec.xcms.org.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.org.repository")
public class OrganizationAutoConfiguration {
}
