package com.df4j.xctec.xcms.kernel.config;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Hibernate 租户标识解析器。
 * 将 TenantContext 中的租户ID传递给 Hibernate 的 @TenantId 机制。
 *
 * <p>由 {@code KernelAutoConfiguration} 以 {@code @Bean} 注册（而非 {@code @Component}），
 * 确保共享内核基础设施被自动装配。此前因无 {@code @ComponentScan} 覆盖本包，该解析器未注册，
 * 导致 Hibernate 多租户在创建 Repository 查询时报 "no tenant identifier specified"。</p>
 *
 * <p>泛型为 {@code Long}，与 {@code @TenantId} 字段类型一致；若返回 String，Hibernate 在
 * 赋值 filter 参数时会因类型不匹配抛 IllegalArgumentException。</p>
 */
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long> {

    /** 无租户上下文时的占位租户标识（系统级/启动期）。 */
    private static final Long DEFAULT_TENANT = 0L;

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
