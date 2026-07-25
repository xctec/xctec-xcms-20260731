package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 重置用户密码请求体
 */
@Data
public class UserResetPasswordRequest {
    private Long id;
    private String newPassword;
}
