package com.df4j.xctec.xcms.task.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsyncTaskDTO {
    @Schema(description = "任务ID")
    private Long id;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "任务类型")
    private String taskType;
    @Schema(description = "任务载荷（JSON 字符串）")
    private String payload;
    @Schema(description = "状态（PENDING/RUNNING/SUCCESS/FAILED）")
    private String status;
    @Schema(description = "优先级")
    private int priority;
    @Schema(description = "计划执行时间")
    private LocalDateTime scheduledAt;
    @Schema(description = "开始执行时间")
    private LocalDateTime startedAt;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "重试次数")
    private int retryCount;
    @Schema(description = "错误信息")
    private String errorMsg;
}
