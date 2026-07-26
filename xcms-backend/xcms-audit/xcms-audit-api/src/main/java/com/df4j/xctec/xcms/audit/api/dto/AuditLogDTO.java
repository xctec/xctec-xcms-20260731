package com.df4j.xctec.xcms.audit.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogDTO {
    @Schema(description = "审计记录ID")
    private Long id;
    @Schema(description = "事件唯一标识（用于链路追踪）")
    private String eventId;
    @Schema(description = "业务模块标识")
    private String bizModule;
    @Schema(description = "事件类型")
    private String eventType;
    @Schema(description = "业务对象ID")
    private String bizId;
    @Schema(description = "操作动作描述")
    private String action;
    @Schema(description = "操作人ID")
    private Long operatorId;
    @Schema(description = "操作人姓名")
    private String operatorName;
    @Schema(description = "操作来源IP")
    private String ip;
    @Schema(description = "是否成功")
    private boolean success;
    @Schema(description = "失败时的错误信息")
    private String errorMsg;
    @Schema(description = "操作详情（JSON 字符串）")
    private String detail;
    @Schema(description = "耗时（毫秒）")
    private long durationMs;
    @Schema(description = "事件发生时间")
    private LocalDateTime occurTime;
    @Schema(description = "记录创建时间")
    private LocalDateTime createdAt;
}
