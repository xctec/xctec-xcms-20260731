package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 更新角色请求
 */
@Data
public class RoleUpdateRequest {
    /** 角色 ID（全 POST 风格，由请求体携带，替代 @PathVariable） */
    private Long id;
    private String roleName;
    private String description;
}
