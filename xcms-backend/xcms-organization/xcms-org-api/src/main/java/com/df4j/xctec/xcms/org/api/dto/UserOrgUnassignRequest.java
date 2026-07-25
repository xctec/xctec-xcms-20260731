package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

/**
 * 用户组织关系解除请求体
 */
@Data
public class UserOrgUnassignRequest {
    private Long userId;
    private Long positionId;
}
