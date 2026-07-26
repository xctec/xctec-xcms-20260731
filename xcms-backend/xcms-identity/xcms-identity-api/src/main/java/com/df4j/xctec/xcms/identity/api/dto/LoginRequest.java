package com.df4j.xctec.xcms.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码（明文传输，请务必配合 HTTPS）")
    private String password;

    @Schema(description = "租户 ID，多租户登录时必填")
    private Long tenantId;

    @Schema(description = "设备类型，如 web/app，用于登录态区分与审计")
    private String deviceType;
}
