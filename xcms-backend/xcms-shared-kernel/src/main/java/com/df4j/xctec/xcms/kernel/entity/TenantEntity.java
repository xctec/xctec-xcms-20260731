package com.df4j.xctec.xcms.kernel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.TenantId;

/**
 * 租户实体基类。所有需要租户隔离的 JPA 实体继承此类。
 * Hibernate 7 的 @TenantId 注解自动处理：
 * - 查询时自动注入 WHERE tenant_id = ?
 * - 插入时自动填充 tenant_id
 * - 更新/删除时自动加 WHERE tenant_id = ?
 */
@MappedSuperclass
public abstract class TenantEntity extends BaseEntity {

    @TenantId
    @Column(name = "tenant_id")
    private Long tenantId;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
