package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Token 解析信息
 */
@Data
public class TokenInfo {
    private Long userId;
    private Long tenantId;
    private String username;
    private LocalDateTime expireAt;
    private boolean valid;
}
