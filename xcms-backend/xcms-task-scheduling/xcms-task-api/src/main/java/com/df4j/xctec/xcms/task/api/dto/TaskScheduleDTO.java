package com.df4j.xctec.xcms.task.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskScheduleDTO {
    @Schema(description = "调度ID")
    private Long id;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "任务名称")
    private String taskName;
    @Schema(description = "任务编码")
    private String taskCode;
    @Schema(description = "任务类型")
    private String taskType;
    @Schema(description = "Cron 表达式")
    private String cronExpression;
    @Schema(description = "固定频率（毫秒）")
    private Long fixedRate;
    @Schema(description = "固定延迟（毫秒）")
    private Long fixedDelay;
    @Schema(description = "最大重试次数")
    private Integer maxRetry;
    @Schema(description = "重试间隔（毫秒）")
    private Long retryInterval;
    @Schema(description = "处理器名称")
    private String handlerName;
    @Schema(description = "处理器参数（JSON 字符串）")
    private String handlerParams;
    @Schema(description = "状态（ENABLED/DISABLED/PAUSED）")
    private String status;
    @Schema(description = "上次执行时间")
    private LocalDateTime lastExecAt;
    @Schema(description = "下次执行时间")
    private LocalDateTime nextExecAt;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
