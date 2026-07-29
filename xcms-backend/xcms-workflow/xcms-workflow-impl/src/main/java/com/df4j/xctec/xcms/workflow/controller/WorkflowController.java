package com.df4j.xctec.xcms.workflow.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.workflow.api.WorkflowService;
import com.df4j.xctec.xcms.workflow.api.dto.WorkflowDefinitionDTO;
import com.df4j.xctec.xcms.workflow.api.dto.WorkflowInstanceDTO;
import com.df4j.xctec.xcms.workflow.api.dto.WorkflowTaskDTO;
import com.df4j.xctec.xcms.workflow.api.dto.request.CompleteCommand;
import com.df4j.xctec.xcms.workflow.api.dto.request.DeployCommand;
import com.df4j.xctec.xcms.workflow.api.dto.request.InstanceQuery;
import com.df4j.xctec.xcms.workflow.api.dto.request.StartCommand;
import com.df4j.xctec.xcms.workflow.api.dto.request.TaskQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作流（业务面）。全 POST 风格。
 */
@RestController
@RequestMapping("/api/workflow")
@Tag(name = "工作流 Workflow", description = "业务面：流程部署/发起/任务办理")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    /**
     * 静态权限码采用声明式鉴权（AT-08，ADR-016）：入口即拦截，权限要求可静态审计；
     * 动态权限码（如 workflow:start:{defKey}）仍保留服务内命令式校验。
     */
    @Operation(summary = "部署流程定义", description = "需要 workflow:deploy 权限")
    @PreAuthorize("hasAuthority('workflow:deploy') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/deploy")
    public ApiResponse<WorkflowDefinitionDTO> deploy(@RequestBody DeployCommand command) {
        return ApiResponse.success(workflowService.deploy(command));
    }

    @Operation(summary = "发起流程实例")
    @PostMapping("/start")
    public ApiResponse<WorkflowInstanceDTO> start(@RequestBody StartCommand command) {
        return ApiResponse.success(workflowService.start(command));
    }

    @Operation(summary = "查询待办任务", description = "分页查询当前用户待办任务")
    @PostMapping("/tasks")
    public ApiResponse<PageResult<WorkflowTaskDTO>> tasks(@RequestBody TaskQuery query) {
        return ApiResponse.success(workflowService.tasks(query));
    }

    @Operation(summary = "办理任务")
    @PostMapping("/complete")
    public ApiResponse<Void> complete(@RequestBody CompleteCommand command) {
        workflowService.complete(command.getTaskId(), command);
        return ApiResponse.success();
    }

    @Operation(summary = "查询流程实例", description = "分页查询流程实例")
    @PostMapping("/instances")
    public ApiResponse<PageResult<WorkflowInstanceDTO>> instances(@RequestBody InstanceQuery query) {
        return ApiResponse.success(workflowService.instances(query));
    }

    @Operation(summary = "查询流程实例详情")
    @PostMapping("/detail")
    public ApiResponse<WorkflowInstanceDTO> detail(@RequestBody IdRequest id) {
        return ApiResponse.success(workflowService.instanceDetail(id.getId()));
    }
}
