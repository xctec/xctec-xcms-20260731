package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 角色信息
 */
@Data
public class RoleDTO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String roleType;
    private String description;
    private Long parentId;
    private String status;
}
