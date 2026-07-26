package com.df4j.xctec.xcms.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MenuDTO {
    @Schema(description = "菜单 ID")
    private Long id;

    @Schema(description = "菜单编码")
    private String menuCode;

    @Schema(description = "权限标识（按钮/菜单对应的真实权限码，用于前端鉴权；目录级可留空）")
    private String permission;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单类型")
    private String menuType;

    @Schema(description = "前端路由路径")
    private String path;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "子菜单列表（树形结构）")
    private List<MenuDTO> children;
}
