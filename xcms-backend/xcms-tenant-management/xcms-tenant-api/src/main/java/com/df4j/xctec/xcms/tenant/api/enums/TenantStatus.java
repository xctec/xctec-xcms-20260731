package com.df4j.xctec.xcms.tenant.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** 租户状态。 */
@Schema(description = "租户状态：ACTIVE-启用，SUSPENDED-停用，LOCKED-锁定，MIGRATING-迁移中，ARCHIVED-归档")
public enum TenantStatus {
    /** 启用 */
    @Schema(description = "启用：租户正常提供服务")
    ACTIVE,
    /** 停用 */
    @Schema(description = "停用：租户被暂停服务")
    SUSPENDED,
    /** 锁定 */
    @Schema(description = "锁定：租户因违规或风险被锁定")
    LOCKED,
    /** 迁移中 */
    @Schema(description = "迁移中：租户正在迁移层级或数据")
    MIGRATING,
    /** 归档 */
    @Schema(description = "归档：租户已归档，仅保留只读数据")
    ARCHIVED
}
