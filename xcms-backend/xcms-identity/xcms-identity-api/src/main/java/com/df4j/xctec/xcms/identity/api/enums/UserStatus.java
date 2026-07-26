package com.df4j.xctec.xcms.identity.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** 用户状态。 */
@Schema(description = "用户状态：ACTIVE-正常，DISABLED-停用，LOCKED-锁定")
public enum UserStatus {
    /** 正常 */
    @Schema(description = "正常：可正常登录与使用系统")
    ACTIVE,
    /** 停用 */
    @Schema(description = "停用：账号被管理员停用，禁止登录")
    DISABLED,
    /** 锁定 */
    @Schema(description = "锁定：因安全策略（如多次密码错误）被锁定")
    LOCKED
}
