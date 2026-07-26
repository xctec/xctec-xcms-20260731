package com.df4j.xctec.xcms.workflow.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 跨租户任务授权。对齐 DDL 表 {@code wf_cross_tenant_task_auth}。
 */
@Entity
@Table(name = "wf_cross_tenant_task_auth")
@Getter
@Setter
public class WfCrossTenantTaskAuth extends TenantEntity {

    @Column(name = "process_instance_id")
    private Long processInstanceId;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "assignee_user_id")
    private Long assigneeUserId;

    @Column(name = "assignee_tenant_id")
    private Long assigneeTenantId;

    @Column(name = "node_config_id")
    private Long nodeConfigId;

    @Column(name = "token", length = 128)
    private String token;

    @Column(name = "status", length = 16)
    private String status = "ACTIVE";

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
