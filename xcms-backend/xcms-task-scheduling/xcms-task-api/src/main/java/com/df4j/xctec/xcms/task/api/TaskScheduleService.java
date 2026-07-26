package com.df4j.xctec.xcms.task.api;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.task.api.dto.TaskExecutionLogDTO;
import com.df4j.xctec.xcms.task.api.dto.TaskScheduleDTO;
import com.df4j.xctec.xcms.task.api.dto.request.TaskCreateRequest;
import com.df4j.xctec.xcms.task.api.dto.request.TaskQuery;
import com.df4j.xctec.xcms.task.api.dto.request.TaskUpdateRequest;

/**
 * 定时任务管理。
 */
public interface TaskScheduleService {

    TaskScheduleDTO createTask(TaskCreateRequest request);

    TaskScheduleDTO updateTask(Long taskId, TaskUpdateRequest request);

    void deleteTask(Long taskId);

    void enableTask(Long taskId);

    void disableTask(Long taskId);

    /** 立即触发一次执行（异步） */
    void triggerTask(Long taskId);

    TaskScheduleDTO getTask(Long taskId);

    PageResult<TaskScheduleDTO> listTasks(TaskQuery query);

    PageResult<TaskExecutionLogDTO> listExecutionLogs(Long taskId, int page, int size);
}
