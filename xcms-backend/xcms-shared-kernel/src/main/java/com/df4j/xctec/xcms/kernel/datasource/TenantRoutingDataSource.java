package com.df4j.xctec.xcms.kernel.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 租户数据源路由。
 * 现阶段：所有租户走共享库（返回 "shared"）。
 * 未来：大租户可走独立库（返回租户专属 key）。
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        // 现阶段：所有租户走共享库
        return "shared";
        // 未来：分库路由由租户元数据（tenant_info.datasource_key）驱动，
        // 不经线程上下文（ActorContext 不承载数据源信息，ADR-012）
    }
}
