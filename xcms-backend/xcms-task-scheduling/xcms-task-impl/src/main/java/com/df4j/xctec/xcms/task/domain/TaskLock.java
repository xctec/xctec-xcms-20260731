package com.df4j.xctec.xcms.task.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 调度器集群选主锁。单行（lock_key='scheduler'）记录当前 Leader 实例。
 */
@Getter
@Setter
@Entity
@Table(name = "task_lock")
public class TaskLock {

    @Id
    @Column(name = "lock_key", length = 64)
    private String lockKey;

    @Column(name = "owner_id", nullable = false, length = 128)
    private String ownerId;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public TaskLock() {
    }

    public TaskLock(String lockKey, String ownerId, LocalDateTime acquiredAt, LocalDateTime expiresAt) {
        this.lockKey = lockKey;
        this.ownerId = ownerId;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }
}
