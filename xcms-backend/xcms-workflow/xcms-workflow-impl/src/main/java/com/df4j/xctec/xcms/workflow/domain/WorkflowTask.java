package com.df4j.xctec.xcms.workflow.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 任务（待办）。租户隔离。flowTaskId 关联 Flowable 任务。
 */
@Entity
@Table(name = "wf_task")
@Getter
@Setter
public class WorkflowTask extends TenantEntity {

    @Column(name = "flow_task_id", length = 64)
    private String flowTaskId;

    @Column(name = "instance_id")
    private Long instanceId;

    @Column(name = "flow_instance_id", length = 64)
    private String flowInstanceId;

    @Column(name = "task_key", length = 64)
    private String taskKey;

    @Column(name = "task_name", length = 128)
    private String taskName;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "candidate_group", length = 64)
    private String candidateGroup;

    @Column(name = "status", length = 16)
    private String status = "PENDING";

    @Column(name = "claim_time")
    private LocalDateTime claimTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Column(name = "comment", length = 512)
    private String comment;

    @Lob
    @Column(name = "form_data")
    private String formData;
}
