package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 登录结果
 */
@Data
public class LoginResult {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private UserDTO user;
    private Boolean forceChangePassword;
}
