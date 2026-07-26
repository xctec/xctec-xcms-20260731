package com.df4j.xctec.xcms.kernel;

import com.df4j.xctec.xcms.kernel.config.TenantIdentifierResolver;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 共享内核基础设施自动配置。
 *
 * <p>注册 Hibernate 多租户标识解析器等全局基础设施 Bean。此前 shared-kernel 的
 * {@code AutoConfiguration.imports} 仅注册 {@link kernel.event.EventPublisherAutoConfiguration}，
 * {@code kernel.config} 包下的 {@code @Component} 未被任何 {@code @ComponentScan} 覆盖，
 * 导致 {@link TenantIdentifierResolver} 缺失，单体无法冷启动。</p>
 */
@Configuration
public class KernelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CurrentTenantIdentifierResolver.class)
    public CurrentTenantIdentifierResolver<Long> tenantIdentifierResolver() {
        return new TenantIdentifierResolver();
    }
}
