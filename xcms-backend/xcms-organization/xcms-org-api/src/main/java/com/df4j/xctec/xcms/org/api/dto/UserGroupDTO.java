package com.df4j.xctec.xcms.org.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户组信息")
@Data
public class UserGroupDTO {
    @Schema(description = "用户组ID")
    private Long id;
    @Schema(description = "用户组名称")
    private String groupName;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "类型（SYSTEM/CUSTOM）")
    private String type;
    @Schema(description = "成员数量")
    private Integer memberCount;
}
