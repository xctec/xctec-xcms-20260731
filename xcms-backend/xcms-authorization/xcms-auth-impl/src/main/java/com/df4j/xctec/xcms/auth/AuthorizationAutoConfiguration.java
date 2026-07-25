package com.df4j.xctec.xcms.auth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 授权模块自动装配。
 *
 * <p>在模块化（非聚合）场景下，引用方应用不会扫描本模块包，需通过该自动配置注册
 * 本模块的 @Component、JPA 实体与 Repository，避免 Bean 未注册导致启动失败。</p>
 */
@ComponentScan("com.df4j.xctec.xcms.auth")
@EntityScan("com.df4j.xctec.xcms.auth.domain")
@EnableJpaRepositories("com.df4j.xctec.xcms.auth.repository")
@AutoConfiguration
public class AuthorizationAutoConfiguration {
}
