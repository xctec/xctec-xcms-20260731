package com.df4j.xctec.xcms.workflow.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 节点配置（办理人范围/角色/超时）。对齐 DDL 表 {@code wf_node_config}。
 */
@Entity
@Table(name = "wf_node_config")
@Getter
@Setter
public class WfNodeConfig extends TenantEntity {

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "node_id", length = 64)
    private String nodeId;

    @Column(name = "node_name", length = 128)
    private String nodeName;

    @Column(name = "node_type", length = 32)
    private String nodeType;

    @Column(name = "participant_scope", length = 16)
    private String participantScope;

    @Column(name = "participant_tenant_id")
    private Long participantTenantId;

    @Column(name = "role_code", length = 64)
    private String roleCode;

    @Column(name = "timeout_hours")
    private Integer timeoutHours;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
