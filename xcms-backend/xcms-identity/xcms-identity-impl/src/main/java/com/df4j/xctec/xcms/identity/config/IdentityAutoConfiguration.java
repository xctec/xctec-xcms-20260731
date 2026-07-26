package com.df4j.xctec.xcms.identity.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 身份模块自动配置：注册组件、实体与 JPA 仓库，并提供 PasswordEncoder。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.df4j.xctec.xcms.identity")
@EntityScan(basePackages = "com.df4j.xctec.xcms.identity.domain")
@EnableJpaRepositories(basePackages = "com.df4j.xctec.xcms.identity.repository")
public class IdentityAutoConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
