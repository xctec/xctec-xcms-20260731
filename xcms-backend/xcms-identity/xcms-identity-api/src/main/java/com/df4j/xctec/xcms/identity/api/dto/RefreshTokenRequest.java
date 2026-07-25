package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 刷新令牌请求体
 */
@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
