package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跨租户授权令牌校验结果（dataScope 即授权可见的资源范围）。
 */
@Data
public class CrossTenantAuthContext {
    private String token;
    private Long sourceTenantId;   // 发起租户
    private Long targetTenantId;   // 目标租户
    private Long userId;
    private String dataScope;      // JSON，授权可见资源范围
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String status;
    private boolean valid;
}
