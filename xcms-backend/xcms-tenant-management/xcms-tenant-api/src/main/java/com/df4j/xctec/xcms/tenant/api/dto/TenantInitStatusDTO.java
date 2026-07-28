package com.df4j.xctec.xcms.tenant.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户初始化状态 DTO
 */
@Data
@Schema(description = "租户初始化状态")
public class TenantInitStatusDTO implements Serializable {

    @Schema(description = "记录 ID")
    private Long id;

    @Schema(description = "租户 ID")
    private Long tenantId;

    @Schema(description = "初始化模块（identity/org 等）")
    private String module;

    @Schema(description = "状态：SUCCESS/FAILED")
    private String status;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
