package com.df4j.xctec.xcms.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XCMS 单体集成启动类（组合根）。
 *
 * <p>本模块不包含任何业务逻辑，仅负责：
 * <ol>
 *   <li>启动 Spring Boot 容器；</li>
 *   <li>通过 classpath 上的各模块 {@code META-INF/spring/*.imports} 自动装配所有业务模块；</li>
 *   <li>提供全局横切配置（数据源、缓存、CORS、租户上下文拦截器、Actuator、OpenAPI）。</li>
 * </ol>
 *
 * <p>注意：{@code @SpringBootApplication} 仅扫描 {@code com.df4j.xctec.xcms.app} 自身包，
 * 业务模块的 Bean / 实体 / Repository 由各自的 {@code *AutoConfiguration} 自注册，互不干扰。</p>
 */
@SpringBootApplication
public class XcmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(XcmsApplication.class, args);
    }
}
