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
 * 异步任务（后台队列）。
 */
@Getter
@Setter
@Entity
@Table(name = "task_async", indexes = {
        @Index(name = "idx_task_async_status", columnList = "tenant_id, status, scheduled_at")
})
public class TaskAsync extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "task_type", nullable = false, length = 64)
    private String taskType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "priority", nullable = false)
    private int priority = 0;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;
}
