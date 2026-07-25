package com.df4j.xctec.xcms.tenant.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 租户管理模块自动配置
 * <p>部署模块引入本模块后，由 Spring Boot 通过 META-INF/spring/AutoConfiguration.imports 自动装配以下 Bean：
 * 服务实现、JPA 仓储、实体扫描与 MapStruct 映射器。可通过 spring.autoconfigure.exclude 关闭本模块。</p>
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.tenant")
@EntityScan(basePackages = "com.df4j.xctec.xcms.tenant.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.tenant.repository")
public class TenantManagementAutoConfiguration {
}
