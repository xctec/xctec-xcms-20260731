package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

/**
 * 权限校验请求体
 */
@Data
public class CheckPermissionRequest {
    private Long userId;
    private String permCode;
}
