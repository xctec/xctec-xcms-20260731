package com.df4j.xctec.xcms.workflow.api;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.workflow.api.dto.WorkflowDefinitionDTO;
import com.df4j.xctec.xcms.workflow.api.dto.WorkflowInstanceDTO;
import com.df4j.xctec.xcms.workflow.api.dto.WorkflowTaskDTO;
import com.df4j.xctec.xcms.workflow.api.dto.request.CompleteCommand;
import com.df4j.xctec.xcms.workflow.api.dto.request.DeployCommand;
import com.df4j.xctec.xcms.workflow.api.dto.request.InstanceQuery;
import com.df4j.xctec.xcms.workflow.api.dto.request.StartCommand;
import com.df4j.xctec.xcms.workflow.api.dto.request.TaskQuery;

/**
 * 工作流服务（基于 Flowable）。为最上层模块，依赖 identity/authorization/message/file-storage/configuration。
 */
public interface WorkflowService {

    /** 部署流程定义（BPMN） */
    WorkflowDefinitionDTO deploy(DeployCommand command);

    /** 启动流程实例 */
    WorkflowInstanceDTO start(StartCommand command);

    /** 当前用户待办任务 */
    PageResult<WorkflowTaskDTO> tasks(TaskQuery query);

    /** 完成任务 */
    void complete(Long taskId, CompleteCommand command);

    /** 流程实例分页查询 */
    PageResult<WorkflowInstanceDTO> instances(InstanceQuery query);

    /** 流程实例详情（含当前任务） */
    WorkflowInstanceDTO instanceDetail(Long instanceId);
}
