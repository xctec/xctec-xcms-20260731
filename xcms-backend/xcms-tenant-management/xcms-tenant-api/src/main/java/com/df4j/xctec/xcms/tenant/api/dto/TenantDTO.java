package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户信息
 */
@Data
public class TenantDTO {
    @Schema(description = "租户 ID")
    private Long id;

    @Schema(description = "租户编码（唯一标识）")
    private String tenantCode;

    @Schema(description = "租户名称")
    private String tenantName;

    private TenantType tenantType;

    @Schema(description = "父租户 ID，用于租户层级")
    private Long parentId;

    @Schema(description = "租户层级（从 1 开始）")
    private Integer level;

    @Schema(description = "租户路径，如 /1/2/3，用于层级查询")
    private String path;

    private TenantStatus status;

    @Schema(description = "部署模式")
    private String deploymentMode;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
