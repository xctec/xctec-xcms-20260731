package com.df4j.xctec.xcms.workflow.service;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.config.api.ConfigService;
import com.df4j.xctec.xcms.file.api.FileStorageService;
import com.df4j.xctec.xcms.identity.api.UserService;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.message.api.MessageService;
import com.df4j.xctec.xcms.message.api.dto.request.SendMessageCommand;
import com.df4j.xctec.xcms.workflow.api.WorkflowService;
import com.df4j.xctec.xcms.workflow.api.dto.WorkflowDefinitionDTO;
import com.df4j.xctec.xcms.workflow.api.dto.WorkflowInstanceDTO;
import com.df4j.xctec.xcms.workflow.api.dto.WorkflowTaskDTO;
import com.df4j.xctec.xcms.workflow.api.dto.request.CompleteCommand;
import com.df4j.xctec.xcms.workflow.api.dto.request.DeployCommand;
import com.df4j.xctec.xcms.workflow.api.dto.request.InstanceQuery;
import com.df4j.xctec.xcms.workflow.api.dto.request.StartCommand;
import com.df4j.xctec.xcms.workflow.api.dto.request.TaskQuery;
import com.df4j.xctec.xcms.workflow.domain.WorkflowDefinition;
import com.df4j.xctec.xcms.workflow.domain.WorkflowInstance;
import com.df4j.xctec.xcms.workflow.domain.WorkflowTask;
import com.df4j.xctec.xcms.workflow.repository.WorkflowDefinitionRepository;
import com.df4j.xctec.xcms.workflow.repository.WorkflowInstanceRepository;
import com.df4j.xctec.xcms.workflow.repository.WorkflowTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 工作流服务实现（基于 Flowable）。整合：
 * - configuration：实例编码生成（nextCode）
 * - file-storage：启动时的附件校验
 * - identity：解析发起人/办理人名称
 * - authorization：启动前权限校验
 * - message：任务生成时通知办理人
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowTaskRepository taskRepository;
    private final ConfigService configService;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final PermissionService permissionService;
    private final MessageService messageService;

    @Override
    @Transactional
    public WorkflowDefinitionDTO deploy(DeployCommand command) {
        Deployment deployment = repositoryService.createDeployment()
                .addString((command.getDefKey() == null ? "process" : command.getDefKey()) + ".bpmn20.xml", command.getBpmnXml())
                .name(command.getDefName())
                .deploy();
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .latestVersion()
                .list()
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCodes.INTERNAL_ERROR, "流程定义解析失败"));

        WorkflowDefinition def = new WorkflowDefinition();
        def.setDefKey(pd.getKey());
        def.setDefName(pd.getName());
        def.setCategory(command.getCategory());
        def.setVersion(pd.getVersion());
        def.setDeployedId(deployment.getId());
        def.setBpmnXml(command.getBpmnXml());
        def.setStatus("ACTIVE");
        return toDefDto(definitionRepository.save(def));
    }

    @Override
    @Transactional
    public WorkflowInstanceDTO start(StartCommand command) {
        Long userId = TenantContext.getCurrentUserId();
        // authorization：启动权限校验
        permissionService.checkPermission(userId, "workflow:start:" + command.getDefKey());
        // file-storage：附件存在性校验
        if (command.getAttachmentFileId() != null) {
            fileStorageService.getFileInfo(command.getAttachmentFileId());
        }

        String instanceCode = configService.nextCode("WF");
        Map<String, Object> vars = command.getVariables() == null ? Map.of() : command.getVariables();
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(command.getDefKey(), command.getBusinessKey(), vars);

        WorkflowInstance instance = new WorkflowInstance();
        instance.setFlowInstanceId(pi.getId());
        instance.setInstanceCode(instanceCode);
        instance.setDefKey(command.getDefKey());
        instance.setBusinessKey(command.getBusinessKey());
        instance.setTitle(command.getTitle());
        instance.setInitiatorId(userId);
        instance.setInitiatorName(resolveName(userId));
        instance.setStatus("RUNNING");
        instance.setStartTime(LocalDateTime.now());
        instance.setAttachmentFileId(command.getAttachmentFileId());
        instance = instanceRepository.save(instance);

        syncTasks(instance);
        return toInstanceDto(instance, true);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<WorkflowTaskDTO> tasks(TaskQuery query) {
        Long uid = TenantContext.getCurrentUserId();
        int page = query.getPage() <= 0 ? 1 : query.getPage();
        int size = query.getSize() <= 0 ? 20 : query.getSize();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<WorkflowTask> result = taskRepository.findByAssigneeId(uid, pageable);
        List<WorkflowTaskDTO> list = result.getContent().stream().map(this::toTaskDto).toList();
        return PageResult.of(list, result.getTotalElements());
    }

    @Override
    @Transactional
    public void complete(Long taskId, CompleteCommand command) {
        WorkflowTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "任务不存在: " + taskId));
        Map<String, Object> vars = command.getVariables() == null ? Map.of() : command.getVariables();
        taskService.complete(task.getFlowTaskId(), vars);
        task.setStatus("COMPLETED");
        task.setCompleteTime(LocalDateTime.now());
        task.setComment(command.getComment());
        taskRepository.save(task);

        WorkflowInstance instance = instanceRepository.findById(task.getInstanceId())
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "流程实例不存在"));
        boolean active = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instance.getFlowInstanceId()).count() > 0;
        if (!active) {
            instance.setStatus("COMPLETED");
            instance.setEndTime(LocalDateTime.now());
            instanceRepository.save(instance);
        } else {
            syncTasks(instance);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<WorkflowInstanceDTO> instances(InstanceQuery query) {
        int page = query.getPage() <= 0 ? 1 : query.getPage();
        int size = query.getSize() <= 0 ? 20 : query.getSize();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<WorkflowInstance> result;
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            result = instanceRepository.findByStatus(query.getStatus(), pageable);
        } else {
            result = instanceRepository.findAll(pageable);
        }
        List<WorkflowInstanceDTO> list = result.getContent().stream().map(i -> toInstanceDto(i, false)).toList();
        return PageResult.of(list, result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowInstanceDTO instanceDetail(Long instanceId) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "流程实例不存在: " + instanceId));
        return toInstanceDto(instance, true);
    }

    /** 同步 Flowable 当前任务到本地任务表，并通知办理人 */
    private void syncTasks(WorkflowInstance instance) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(instance.getFlowInstanceId()).list();
        for (Task t : tasks) {
            if (taskRepository.findByFlowTaskId(t.getId()).isPresent()) {
                continue;
            }
            WorkflowTask wt = new WorkflowTask();
            wt.setInstanceId(instance.getId());
            wt.setFlowInstanceId(instance.getFlowInstanceId());
            wt.setFlowTaskId(t.getId());
            wt.setTaskKey(t.getTaskDefinitionKey());
            wt.setTaskName(t.getName());
            wt.setAssigneeId(parseLongSafe(t.getAssignee()));
            wt.setStatus("PENDING");
            wt = taskRepository.save(wt);
            notifyAssignee(wt);
        }
    }

    private void notifyAssignee(WorkflowTask task) {
        if (task.getAssigneeId() == null) {
            return;
        }
        try {
            SendMessageCommand cmd = new SendMessageCommand();
            cmd.setTitle("待办任务：" + (task.getTaskName() == null ? "流程任务" : task.getTaskName()));
            cmd.setContent("您有一条新的待办任务，请及时处理。");
            cmd.setRecipientIds(List.of(task.getAssigneeId()));
            cmd.setMsgType("WORKFLOW");
            messageService.send(cmd);
        } catch (Exception e) {
            log.warn("工作流任务通知发送失败: {}", e.getMessage());
        }
    }

    private String resolveName(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return userService.getUserById(userId).getRealName();
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLongSafe(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private WorkflowDefinitionDTO toDefDto(WorkflowDefinition d) {
        WorkflowDefinitionDTO dto = new WorkflowDefinitionDTO();
        dto.setId(d.getId());
        dto.setDefKey(d.getDefKey());
        dto.setDefName(d.getDefName());
        dto.setCategory(d.getCategory());
        dto.setVersion(d.getVersion());
        dto.setStatus(d.getStatus());
        return dto;
    }

    private WorkflowInstanceDTO toInstanceDto(WorkflowInstance i, boolean withTasks) {
        WorkflowInstanceDTO dto = new WorkflowInstanceDTO();
        dto.setId(i.getId());
        dto.setInstanceCode(i.getInstanceCode());
        dto.setDefKey(i.getDefKey());
        dto.setBusinessKey(i.getBusinessKey());
        dto.setTitle(i.getTitle());
        dto.setInitiatorId(i.getInitiatorId());
        dto.setInitiatorName(i.getInitiatorName());
        dto.setStatus(i.getStatus());
        dto.setStartTime(i.getStartTime());
        dto.setEndTime(i.getEndTime());
        dto.setAttachmentFileId(i.getAttachmentFileId());
        if (withTasks) {
            dto.setCurrentTasks(taskRepository.findByInstanceId(i.getId()).stream().map(this::toTaskDto).toList());
        }
        return dto;
    }

    private WorkflowTaskDTO toTaskDto(WorkflowTask t) {
        WorkflowTaskDTO dto = new WorkflowTaskDTO();
        dto.setId(t.getId());
        dto.setTaskKey(t.getTaskKey());
        dto.setTaskName(t.getTaskName());
        dto.setAssigneeId(t.getAssigneeId());
        dto.setCandidateGroup(t.getCandidateGroup());
        dto.setStatus(t.getStatus());
        dto.setClaimTime(t.getClaimTime());
        dto.setCompleteTime(t.getCompleteTime());
        dto.setComment(t.getComment());
        return dto;
    }
}
