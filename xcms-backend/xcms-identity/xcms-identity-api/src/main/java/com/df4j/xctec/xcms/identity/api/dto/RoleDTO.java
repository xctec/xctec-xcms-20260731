package com.df4j.xctec.xcms.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色信息
 */
@Data
public class RoleDTO {
    @Schema(description = "角色 ID")
    private Long id;

    @Schema(description = "角色编码（唯一标识）")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色类型")
    private String roleType;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "父角色 ID，用于角色层级")
    private Long parentId;

    @Schema(description = "角色状态")
    private String status;
}
