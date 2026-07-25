package com.df4j.xctec.xcms.workflow.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 流程实例。租户隔离。flowInstanceId 关联 Flowable 运行时实例。
 */
@Entity
@Table(name = "wf_instance")
@Getter
@Setter
public class WorkflowInstance extends TenantEntity {

    @Column(name = "flow_instance_id", length = 64)
    private String flowInstanceId;

    @Column(name = "instance_code", length = 64)
    private String instanceCode;

    @Column(name = "def_key", length = 64)
    private String defKey;

    @Column(name = "business_key", length = 64)
    private String businessKey;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "initiator_id")
    private Long initiatorId;

    @Column(name = "initiator_name", length = 64)
    private String initiatorName;

    @Column(name = "status", length = 16)
    private String status = "RUNNING";

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "attachment_file_id")
    private Long attachmentFileId;
}
