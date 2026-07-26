package com.df4j.xctec.xcms.task.api.event;

/**
 * 定时任务执行失败领域事件。
 * <p>
 * 由任务调度引擎在任务最终失败（重试耗尽）时发布，供上层业务模块（如 message）监听并发送告警，
 * 从而解除 task-scheduling 对 message-api 的直接依赖（底层基础设施不应依赖上层业务模块）。
 */
public class TaskFailedEvent {

    private final Long taskId;
    private final Long tenantId;
    private final String taskCode;
    private final String taskName;
    private final String errorMessage;

    public TaskFailedEvent(Long taskId, Long tenantId, String taskCode, String taskName, String errorMessage) {
        this.taskId = taskId;
        this.tenantId = tenantId;
        this.taskCode = taskCode;
        this.taskName = taskName;
        this.errorMessage = errorMessage;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
