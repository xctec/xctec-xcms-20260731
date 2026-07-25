package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
    private Long tenantId;
    private String deviceType;
}
