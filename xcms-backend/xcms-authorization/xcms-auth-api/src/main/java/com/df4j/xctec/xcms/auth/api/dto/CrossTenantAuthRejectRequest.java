package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

/**
 * 跨租户授权驳回请求体
 */
@Data
public class CrossTenantAuthRejectRequest {
    private Long id;
    private Long approverId;
    private String reason;
}
