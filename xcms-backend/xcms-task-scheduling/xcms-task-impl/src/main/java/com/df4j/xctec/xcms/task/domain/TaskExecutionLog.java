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
 * 任务执行日志。
 */
@Getter
@Setter
@Entity
@Table(name = "task_execution_log", indexes = {
        @Index(name = "idx_task_log_task", columnList = "tenant_id, task_id"),
        @Index(name = "idx_task_log_status", columnList = "status")
})
public class TaskExecutionLog extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_name", length = 128)
    private String taskName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "RUNNING";

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;
}
