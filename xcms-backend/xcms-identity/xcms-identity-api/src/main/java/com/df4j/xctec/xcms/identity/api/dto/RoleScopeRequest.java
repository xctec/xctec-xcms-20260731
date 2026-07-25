package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 按作用域查询角色请求体
 */
@Data
public class RoleScopeRequest {
    private String scope;
}
