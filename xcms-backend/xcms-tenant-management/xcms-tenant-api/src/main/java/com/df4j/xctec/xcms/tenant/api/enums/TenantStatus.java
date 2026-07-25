package com.df4j.xctec.xcms.tenant.api.enums;

/**
 * 租户状态
 */
public enum TenantStatus {
    /** 启用 */
    ACTIVE,
    /** 停用 */
    SUSPENDED,
    /** 锁定 */
    LOCKED,
    /** 迁移中 */
    MIGRATING,
    /** 归档 */
    ARCHIVED
}
