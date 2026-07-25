package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 创建角色请求
 */
@Data
public class RoleCreateRequest {
    private String roleCode;
    private String roleName;
    private String roleType;
    private String description;
    private Long parentId;
    private String status;
}
