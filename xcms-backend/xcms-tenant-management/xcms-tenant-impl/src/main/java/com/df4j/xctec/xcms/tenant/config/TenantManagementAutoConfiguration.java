package com.df4j.xctec.xcms.tenant.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 租户管理模块自动配置
 * <p>部署模块引入本模块后，由 Spring Boot 自动装配以下 Bean：
 * 服务实现、JPA 仓储、实体扫描与 MapStruct 映射器。</p>
 */
@Configuration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.tenant")
@EntityScan(basePackages = "com.df4j.xctec.xcms.tenant.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.tenant.repository")
public class TenantManagementAutoConfiguration {
}
