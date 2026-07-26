package com.df4j.xctec.xcms.workflow.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 流程模板定义。对齐 DDL 表 {@code wf_process_template}。
 * 部署成功后由 Flowable 回填 {@code procDefId}（流程定义 ID）与 {@code version}。
 */
@Entity
@Table(name = "wf_process_template")
@Getter
@Setter
public class WorkflowDefinition extends TenantEntity {

    /** 流程定义 KEY（业务标识），对应 Flowable processDefinitionKey */
    @Column(name = "proc_def_key", length = 64, nullable = false, unique = true)
    private String procDefKey;

    /** Flowable 流程定义 ID（部署成功后回填） */
    @Column(name = "proc_def_id", length = 64)
    private String procDefId;

    @Column(name = "template_name", length = 128, nullable = false)
    private String templateName;

    @Column(name = "category_id")
    private Long categoryId;

    /** 作用域：TENANT=租户模板，GLOBAL=全局模板 */
    @Column(name = "scope", length = 16, nullable = false)
    private String scope = "TENANT";

    @Column(name = "parent_template_id")
    private Long parentTemplateId;

    @Column(name = "version", nullable = false)
    private int version = 1;

    /** DRAFT / PUBLISHED / DISABLED */
    @Column(name = "status", length = 16, nullable = false)
    private String status = "DRAFT";

    @Column(name = "bpmn_xml", columnDefinition = "longtext", nullable = false)
    private String bpmnXml;

    @Column(name = "form_config", columnDefinition = "longtext")
    private String formConfig;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
