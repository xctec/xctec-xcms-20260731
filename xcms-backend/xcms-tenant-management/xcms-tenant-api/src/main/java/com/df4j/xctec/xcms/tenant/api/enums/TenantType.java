package com.df4j.xctec.xcms.tenant.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** 租户类型。 */
@Schema(description = "租户类型：ORGANIZATION-组织型，PROJECT-项目型，EXTERNAL-外部型，PLATFORM-平台型(根租户)")
public enum TenantType {
    /** 组织型租户 */
    @Schema(description = "组织型租户：面向企业/组织单位的租户")
    ORGANIZATION,
    /** 项目型租户 */
    @Schema(description = "项目型租户：面向单个项目生命周期的租户")
    PROJECT,
    /** 外部型租户 */
    @Schema(description = "外部型租户：面向外部合作伙伴/客户的租户")
    EXTERNAL,
    /** 平台型租户（根租户） */
    @Schema(description = "平台型租户：平台根租户，承载多租户隔离的根")
    PLATFORM
}
