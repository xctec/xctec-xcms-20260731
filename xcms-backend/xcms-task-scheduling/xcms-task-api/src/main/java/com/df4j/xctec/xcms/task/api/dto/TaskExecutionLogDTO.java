package com.df4j.xctec.xcms.task.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskExecutionLogDTO {
    @Schema(description = "日志ID")
    private Long id;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "关联任务ID")
    private Long taskId;
    @Schema(description = "任务名称")
    private String taskName;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    @Schema(description = "执行状态（SUCCESS/FAILED）")
    private String status;
    @Schema(description = "执行结果（JSON 字符串）")
    private String result;
    @Schema(description = "错误信息")
    private String errorMsg;
    @Schema(description = "重试次数")
    private int retryCount;
}
