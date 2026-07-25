package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

/**
 * 跨租户授权审批请求体
 */
@Data
public class CrossTenantAuthApproveRequest {
    private Long id;
    private Long approverId;
}
