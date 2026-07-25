package com.df4j.xctec.xcms.workflow.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 流程定义。租户隔离。部署后记录 Flowable 的 deployedId 与版本。
 */
@Entity
@Table(name = "wf_definition")
@Getter
@Setter
public class WorkflowDefinition extends TenantEntity {

    @Column(name = "def_key", length = 64)
    private String defKey;

    @Column(name = "def_name", length = 128)
    private String defName;

    @Column(name = "category", length = 64)
    private String category;

    @Column(name = "version")
    private int version;

    @Column(name = "deployed_id", length = 64)
    private String deployedId;

    @Lob
    @Column(name = "bpmn_xml")
    private String bpmnXml;

    @Column(name = "status", length = 16)
    private String status = "ACTIVE";
}
