# API Design: Workflow

> 流程中心 API 接口定义

## 1. workflow-api 接口定义

### 1.1 ProcessService（流程发起与管理）

```java
package com.xcms.workflow.api;

public interface ProcessService {

    /**
     * 发起流程
     * @param procDefKey 流程定义Key
     * @param variables 流程变量
     * @return 流程实例信息
     */
    ProcessInstanceDTO startProcess(String procDefKey, Map<String, Object> variables);

    /**
     * 获取流程实例详情
     */
    ProcessInstanceDTO getProcessInstance(String processInstanceId);

    /**
     * 撤回流程（发起人操作）
     */
    void cancelProcess(String processInstanceId, String reason);

    /**
     * 挂起流程（管理员操作）
     */
    void suspendProcess(String processInstanceId);

    /**
     * 恢复流程
     */
    void resumeProcess(String processInstanceId);

    /**
     * 终止流程（管理员操作）
     */
    void terminateProcess(String processInstanceId, String reason);

    /**
     * 查询流程实例列表
     */
    PageResult<ProcessInstanceDTO> listProcessInstances(ProcessInstanceQuery query);

    /**
     * 获取流程审批记录
     */
    List<ProcessHistoryDTO> getProcessHistory(String processInstanceId);

    /**
     * 催办（提醒当前审批人）
     */
    void urgeProcess(String processInstanceId, String message);
}
```

### 1.2 TaskService（任务处理）

```java
package com.xcms.workflow.api;

public interface TaskService {

    /**
     * 获取我的待办任务（跨租户，用户维度查询）
     * @param userId 用户ID
     * @return 待办列表，包含跨租户标记
     */
    PageResult<TaskDTO> getMyTasks(Long userId, TaskQuery query);

    /**
     * 获取我的已办任务
     */
    PageResult<TaskDTO> getMyCompletedTasks(Long userId, TaskQuery query);

    /**
     * 获取我发起的流程
     */
    PageResult<ProcessInstanceDTO> getMyProcesses(Long userId, ProcessInstanceQuery query);

    /**
     * 获取任务详情（含跨租户数据过滤）
     */
    TaskDetailDTO getTaskDetail(String taskId, Long userId);

    /**
     * 完成任务（审批通过）
     */
    void completeTask(String taskId, Long userId, TaskCompleteRequest request);

    /**
     * 驳回任务
     */
    void rejectTask(String taskId, Long userId, String comment);

    /**
     * 转办任务
     */
    void delegateTask(String taskId, Long userId, Long targetUserId);

    /**
     * 加签（增加审批人）
     */
    void addAssignee(String taskId, Long userId, List<Long> additionalUserIds);

    /**
     * 获取任务审批记录
     */
    List<TaskCommentDTO> getTaskComments(String taskId);
}
```

### 1.3 ProcessTemplateService（流程模板管理，管理面调用）

```java
package com.xcms.workflow.api;

public interface ProcessTemplateService {

    /**
     * 创建流程模板
     */
    ProcessTemplateDTO createTemplate(TemplateCreateRequest request);

    /**
     * 更新流程模板
     */
    ProcessTemplateDTO updateTemplate(Long templateId, TemplateUpdateRequest request);

    /**
     * 发布流程模板
     */
    ProcessTemplateDTO publishTemplate(Long templateId);

    /**
     * 停用流程模板
     */
    void disableTemplate(Long templateId);

    /**
     * 获取模板详情
     */
    ProcessTemplateDTO getTemplate(Long templateId);

    /**
     * 获取模板列表
     */
    PageResult<ProcessTemplateDTO> listTemplates(TemplateQuery query);

    /**
     * 获取可发起的流程列表（当前租户可见的模板）
     */
    List<ProcessTemplateDTO> getAvailableTemplates();

    /**
     * 获取模板版本历史
     */
    List<ProcessTemplateDTO> getTemplateVersions(String procDefKey);

    /**
     * 保存流程节点配置
     */
    void saveNodeConfigs(Long templateId, List<NodeConfigDTO> nodeConfigs);

    /**
     * 获取流程节点配置
     */
    List<NodeConfigDTO> getNodeConfigs(Long templateId);
}
```

### 1.4 WorkflowCategoryService（流程分类）

```java
package com.xcms.workflow.api;

public interface WorkflowCategoryService {
    CategoryDTO createCategory(CategoryCreateRequest request);
    void deleteCategory(Long categoryId);
    List<CategoryDTO> listCategories();
    List<CategoryTreeDTO> getCategoryTree();
}
```

### 1.5 ProcessMonitorService（流程监控，管理面调用）

```java
package com.xcms.workflow.api;

public interface ProcessMonitorService {

    /**
     * 获取运行中流程实例（含超时预警）
     */
    PageResult<ProcessInstanceDTO> listRunningInstances(ProcessInstanceQuery query);

    /**
     * 获取异常流程实例（超时/报错/卡住）
     */
    PageResult<ProcessInstanceDTO> listAbnormalInstances(ProcessInstanceQuery query);

    /**
     * 流程统计
     */
    ProcessStatisticsDTO getStatistics();

    /**
     * 获取超时任务列表
     */
    List<TaskDTO> getOverdueTasks();
}
```

### 1.6 DTO 定义

```java
package com.xcms.workflow.api.dto;

public class ProcessInstanceDTO {
    private String id;                  // Flowable流程实例ID
    private String processDefinitionKey;
    private String processDefinitionName;
    private Long tenantId;              // 归属租户
    private String tenantName;
    private Long startUserId;
    private String startUserName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;              // RUNNING/COMPLETED/CANCELLED/SUSPENDED/TERMINATED
    private String businessKey;         // 业务关联Key
    private String title;               // 流程标题
    private String currentTaskName;     // 当前任务节点名称
    private Boolean overdue;            // 是否超时
}

public class TaskDTO {
    private String id;                  // Flowable任务ID
    private String processInstanceId;
    private String taskName;
    private String title;
    private Long assigneeId;
    private String assigneeName;
    private Long ownerTenantId;         // 流程归属租户
    private Boolean crossTenant;        // 是否跨租户任务
    private LocalDateTime createTime;
    private LocalDateTime dueDate;      // 到期时间
    private Boolean overdue;
    private String status;              // PENDING/COMPLETED/REJECTED
}

public class TaskDetailDTO {
    private TaskDTO task;
    private ProcessInstanceDTO processInstance;
    private Map<String, Object> formData;       // 表单数据（已按权限过滤）
    private List<TaskCommentDTO> comments;      // 审批记录
    private List<NodeConfigDTO> nodeConfigs;    // 节点配置
    private Boolean crossTenant;
    private List<String> visibleFields;         // 可见字段（跨租户时）
}

public class TaskCompleteRequest {
    private String comment;             // 审批意见
    private Map<String, Object> formData; // 表单数据
    private Map<String, Object> variables; // 流程变量
}

public class TaskCommentDTO {
    private String id;
    private String taskId;
    private Long userId;
    private String userName;
    private String action;              // APPROVE/REJECT/DELEGATE/ADD_ASSIGNEE
    private String comment;
    private LocalDateTime time;
}

public class ProcessHistoryDTO {
    private String activityId;
    private String activityName;
    private String activityType;        // START/USER_TASK/GATEWAY/END
    private Long assigneeId;
    private String assigneeName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String action;
    private String comment;
}

public class ProcessTemplateDTO {
    private Long id;
    private String procDefKey;
    private String procDefId;
    private String templateName;
    private Long categoryId;
    private String categoryName;
    private String scope;               // GLOBAL/TENANT
    private Integer version;
    private String status;              // DRAFT/PUBLISHED/DISABLED
    private String bpmnXml;
    private String formConfig;          // JSON
    private String description;
    private LocalDateTime publishedAt;
}

public class TemplateCreateRequest {
    private String procDefKey;
    private String templateName;
    private Long categoryId;
    private String scope;               // GLOBAL/TENANT
    private String bpmnXml;
    private String formConfig;
    private String description;
    private List<NodeConfigDTO> nodeConfigs;
}

public class NodeConfigDTO {
    private Long id;
    private Long templateId;
    private String nodeId;              // BPMN节点ID
    private String nodeName;
    private String nodeType;            // START/APPROVAL/CC/GATEWAY/SUBPROCESS/BRIDGE/END
    private String participantScope;    // INITIATOR/SPECIFIED/PARENT/DYNAMIC
    private Long participantTenantId;
    private String roleCode;
    private String dataPermission;      // JSON, 可见字段
    private String operationPermission; // JSON
    private Integer timeoutHours;
    private String timeoutAction;
}

public class ProcessInstanceQuery extends PageQuery {
    private String keyword;
    private String processDefinitionKey;
    private String status;
    private Long startUserId;
    private Long tenantId;
    private LocalDateTime startTimeFrom;
    private LocalDateTime startTimeTo;
    private Boolean overdue;
}

public class TaskQuery extends PageQuery {
    private String keyword;
    private String taskName;
    private Boolean crossTenant;
    private Boolean overdue;
}

public class ProcessStatisticsDTO {
    private long totalRunning;
    private long totalCompleted;
    private long totalOverdue;
    private long totalToday;
    private double avgProcessDuration;  // 平均处理时长（小时）
    private List<NodeEfficiencyDTO> nodeEfficiencies; // 节点效率分析
}

public class NodeEfficiencyDTO {
    private String nodeName;
    private long totalTasks;
    private double avgDuration;         // 平均处理时长
    private long overdueCount;
}

public class CategoryDTO {
    private Long id;
    private String categoryCode;
    private String categoryName;
    private Long parentId;
    private Integer sortOrder;
}

public class CategoryTreeDTO {
    private Long id;
    private String categoryName;
    private List<CategoryTreeDTO> children;
}
```

### 1.7 事件定义

```java
package com.xcms.workflow.api.event;

public class ProcessStartedEvent implements Serializable {
    private String processInstanceId;
    private Long tenantId;
    private Long startUserId;
    private String processDefinitionKey;
}

public class TaskCompletedEvent implements Serializable {
    private String taskId;
    private String processInstanceId;
    private Long userId;
    private Long tenantId;
    private String action;  // APPROVE/REJECT/DELEGATE
}

public class TaskAssignedEvent implements Serializable {
    private String taskId;
    private String processInstanceId;
    private Long assigneeId;
    private Long assigneeTenantId;
    private Long ownerTenantId;
    private Boolean crossTenant;
}
```

---

## 2. 跨租户任务处理核心逻辑

```java
// TaskServiceImpl 关键逻辑

public TaskDetailDTO getTaskDetail(String taskId, Long userId) {
    Task task = flowableTaskService.createTaskQuery()
        .taskId(taskId)
        .taskAssignee(userId.toString())
        .singleResult();

    if (task == null) {
        throw new PermissionException("无权访问此任务");
    }

    ProcessInstance instance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(task.getProcessInstanceId())
        .singleResult();

    Long ownerTenantId = Long.valueOf(instance.getTenantId());
    Long userTenantId = TenantContext.getTenantId();
    boolean isCrossTenant = !ownerTenantId.equals(userTenantId);

    // 获取流程变量
    Map<String, Object> formData = runtimeService
        .getVariables(task.getProcessInstanceId());

    if (isCrossTenant) {
        // 跨租户任务：按节点配置过滤可见字段
        NodeConfigDTO nodeConfig = getNodeConfig(task);
        formData = filterFields(formData, nodeConfig.getDataPermission());
        // 记录跨租户访问审计
        auditService.logCrossTenantAccess(
            userTenantId, ownerTenantId,
            task.getProcessInstanceId(), "read"
        );
    }

    return new TaskDetailDTO(
        toTaskDTO(task, isCrossTenant),
        toProcessInstanceDTO(instance),
        formData,
        getComments(taskId),
        getNodeConfigs(instance),
        isCrossTenant,
        isCrossTenant ? getVisibleFields(task) : null
    );
}

// 跨租户待办查询（用户维度，不加租户过滤）
public PageResult<TaskDTO> getMyTasks(Long userId, TaskQuery query) {
    var taskQuery = flowableTaskService.createTaskQuery()
        .taskAssignee(userId.toString())
        // 不加 .tenantId() → 跨租户查询所有
        .orderByTaskCreateTime().desc();

    // ... 分页查询
    List<Task> tasks = taskQuery.listPage(offset, query.getSize());

    List<TaskDTO> result = tasks.stream().map(task -> {
        ProcessInstance instance = ...;
        Long ownerTenantId = Long.valueOf(instance.getTenantId());
        Long userTenantId = TenantContext.getTenantId();
        boolean crossTenant = !ownerTenantId.equals(userTenantId);

        TaskDTO dto = toTaskDTO(task, crossTenant);
        dto.setOwnerTenantId(ownerTenantId);
        dto.setCrossTenant(crossTenant);
        return dto;
    }).toList();

    return PageResult.of(result, total, query.getPage(), query.getSize());
}
```

---

## 3. DDL

参见 [db/ddl/04-workflow.sql](../../db/ddl/04-workflow.sql)

> 注意：Flowable 引擎自带的 `ACT_*` 系列表由引擎自动创建，DDL 中仅包含 XCMS 自定义扩展表。
