package com.df4j.xctec.xcms.kernel.config;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Hibernate 租户标识解析器。
 * 将 TenantContext 中的租户ID传递给 Hibernate 的 @TenantId 机制。
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    /**
     * 无租户上下文时的占位租户标识。
     * 必须为可被 Long 解析的合法值，否则系统级查询（如 tenant_info）在 Hibernate 将字符串
     * 转为 @TenantId 的 Long 类型时会抛出 NumberFormatException。
     * 系统级实体（无 tenant_id）不应继承 TenantEntity，本值仅作为未显式设置租户时的兜底。
     */
    private static final String DEFAULT_TENANT = "0";

    @Override
    public String resolveCurrentTenantIdentifier() {
        Long tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId.toString() : DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
