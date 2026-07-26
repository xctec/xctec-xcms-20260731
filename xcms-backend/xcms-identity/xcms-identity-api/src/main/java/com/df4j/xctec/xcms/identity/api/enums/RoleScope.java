package com.df4j.xctec.xcms.identity.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** 角色授权范围。 */
@Schema(description = "角色授权范围：TENANT-租户级，DEPT-部门级，CUSTOM-自定义范围")
public enum RoleScope {
    /** 租户级 */
    @Schema(description = "租户级：角色在所属租户范围内生效")
    TENANT,
    /** 部门级 */
    @Schema(description = "部门级：角色仅在指定部门范围内生效")
    DEPT,
    /** 自定义 */
    @Schema(description = "自定义：角色在调用方指定的 scopeValue 范围内生效")
    CUSTOM
}
