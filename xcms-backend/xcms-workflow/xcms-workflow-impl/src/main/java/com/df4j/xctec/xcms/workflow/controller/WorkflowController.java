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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作流（业务面）。全 POST 风格。
 */
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping("/deploy")
    public ApiResponse<WorkflowDefinitionDTO> deploy(@RequestBody DeployCommand command) {
        return ApiResponse.success(workflowService.deploy(command));
    }

    @PostMapping("/start")
    public ApiResponse<WorkflowInstanceDTO> start(@RequestBody StartCommand command) {
        return ApiResponse.success(workflowService.start(command));
    }

    @PostMapping("/tasks")
    public ApiResponse<PageResult<WorkflowTaskDTO>> tasks(@RequestBody TaskQuery query) {
        return ApiResponse.success(workflowService.tasks(query));
    }

    @PostMapping("/complete")
    public ApiResponse<Void> complete(@RequestBody CompleteCommand command) {
        workflowService.complete(command.getTaskId(), command);
        return ApiResponse.success();
    }

    @PostMapping("/instances")
    public ApiResponse<PageResult<WorkflowInstanceDTO>> instances(@RequestBody InstanceQuery query) {
        return ApiResponse.success(workflowService.instances(query));
    }

    @PostMapping("/detail")
    public ApiResponse<WorkflowInstanceDTO> detail(@RequestBody IdRequest id) {
        return ApiResponse.success(workflowService.instanceDetail(id.getId()));
    }
}
