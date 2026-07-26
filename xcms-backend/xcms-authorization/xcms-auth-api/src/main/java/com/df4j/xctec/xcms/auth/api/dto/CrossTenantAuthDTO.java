package com.df4j.xctec.xcms.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CrossTenantAuthDTO {
    @Schema(description = "跨租户授权 ID")
    private Long id;

    @Schema(description = "来源租户 ID")
    private Long tenantId;

    @Schema(description = "目标租户 ID")
    private Long targetTenantId;

    @Schema(description = "被授权用户 ID")
    private Long userId;

    @Schema(description = "被授权用户名称")
    private String userName;

    @Schema(description = "数据范围")
    private String dataScope;

    @Schema(description = "授权令牌")
    private String token;

    @Schema(description = "生效时间")
    private LocalDateTime validFrom;

    @Schema(description = "失效时间")
    private LocalDateTime validUntil;

    @Schema(description = "授权状态")
    private String status;

    @Schema(description = "审批人 ID")
    private Long approvedBy;

    @Schema(description = "申请原因")
    private String reason;
}
