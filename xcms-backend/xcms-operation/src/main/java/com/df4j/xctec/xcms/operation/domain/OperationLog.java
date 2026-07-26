package com.df4j.xctec.xcms.operation.domain;

import com.df4j.xctec.xcms.kernel.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ops_operation_log", indexes = {
        @Index(name = "idx_ops_op_log_time", columnList = "occur_time"),
        @Index(name = "idx_ops_op_log_operator", columnList = "operator_id, occur_time")
})
@Getter
@Setter
public class OperationLog extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 128)
    private String operatorName;

    @Column(name = "action", length = 128)
    private String action;

    @Column(name = "module", length = 64)
    private String module;

    @Column(name = "biz_type", length = 64)
    private String bizType;

    @Column(name = "biz_id", length = 64)
    private String bizId;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "result", nullable = false, length = 16)
    private String result;

    @Column(name = "error_msg", length = 1024)
    private String errorMsg;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "occur_time", nullable = false)
    private LocalDateTime occurTime;
}
