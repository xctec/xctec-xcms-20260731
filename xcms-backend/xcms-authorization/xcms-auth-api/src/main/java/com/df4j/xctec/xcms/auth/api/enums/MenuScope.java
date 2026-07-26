package com.df4j.xctec.xcms.auth.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** 菜单作用范围。 */
@Schema(description = "菜单作用范围：ADMIN-管理后台菜单，BUSINESS-业务前台菜单，BOTH-管理后台与业务前台通用菜单")
public enum MenuScope {
    @Schema(description = "管理后台菜单：仅出现在管理后台导航")
    ADMIN,
    @Schema(description = "业务前台菜单：仅出现在业务前台导航")
    BUSINESS,
    @Schema(description = "管理后台与业务前台通用菜单")
    BOTH
}
