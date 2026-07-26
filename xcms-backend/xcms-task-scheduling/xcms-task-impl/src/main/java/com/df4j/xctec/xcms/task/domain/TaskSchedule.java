package com.df4j.xctec.xcms.task.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 定时任务。系统级（跨租户共享），tenant_id 可空。
 */
@Getter
@Setter
@Entity
@Table(name = "task_schedule", indexes = {
        @Index(name = "uk_task_code", columnList = "tenant_id, task_code", unique = true)
})
public class TaskSchedule extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "task_name", nullable = false, length = 128)
    private String taskName;

    @Column(name = "task_code", nullable = false, length = 128)
    private String taskCode;

    @Column(name = "task_type", nullable = false, length = 20)
    private String taskType;

    @Column(name = "cron_expression", length = 128)
    private String cronExpression;

    @Column(name = "fixed_rate")
    private Long fixedRate;

    @Column(name = "fixed_delay")
    private Long fixedDelay;

    @Column(name = "max_retry")
    private Integer maxRetry;

    @Column(name = "retry_interval")
    private Long retryInterval;

    @Column(name = "handler_class", nullable = false, length = 256)
    private String handlerName;

    @Column(name = "handler_params", columnDefinition = "TEXT")
    private String handlerParams;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ENABLED";

    @Column(name = "last_exec_at")
    private LocalDateTime lastExecAt;

    @Column(name = "next_exec_at")
    private LocalDateTime nextExecAt;

    @Column(name = "description", length = 512)
    private String description;
}
