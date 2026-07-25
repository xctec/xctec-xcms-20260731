package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 令牌请求体（校验 / 登出 / 吊销）
 */
@Data
public class TokenRequest {
    private String token;
}
