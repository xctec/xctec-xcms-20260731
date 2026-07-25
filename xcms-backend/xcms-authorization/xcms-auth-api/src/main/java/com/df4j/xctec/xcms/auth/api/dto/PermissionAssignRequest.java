package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

@Data
public class PermissionAssignRequest {
    private Long permId;
    private String permType;
    private String scopeConfig;
}
