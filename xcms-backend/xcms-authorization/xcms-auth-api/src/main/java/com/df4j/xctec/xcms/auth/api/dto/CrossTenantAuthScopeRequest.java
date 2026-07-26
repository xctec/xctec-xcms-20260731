package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

@Data
public class CrossTenantAuthScopeRequest {
    private Long id;
    private String dataScope;
}
