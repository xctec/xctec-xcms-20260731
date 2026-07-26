package com.df4j.xctec.xcms.task.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.task.api.TaskAsyncService;
import com.df4j.xctec.xcms.task.api.TaskScheduleService;
import com.df4j.xctec.xcms.task.api.dto.AsyncTaskDTO;
import com.df4j.xctec.xcms.task.api.dto.TaskExecutionLogDTO;
import com.df4j.xctec.xcms.task.api.dto.TaskScheduleDTO;
import com.df4j.xctec.xcms.task.api.dto.request.AsyncTaskQuery;
import com.df4j.xctec.xcms.task.api.dto.request.AsyncTaskRequest;
import com.df4j.xctec.xcms.task.api.dto.request.TaskCreateRequest;
import com.df4j.xctec.xcms.task.api.dto.request.TaskQuery;
import com.df4j.xctec.xcms.task.api.dto.request.TaskUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskScheduleService scheduleService;
    private final TaskAsyncService asyncService;

    @PostMapping("/schedule/create")
    public ApiResponse<TaskScheduleDTO> create(@RequestBody TaskCreateRequest request) {
        return ApiResponse.success(scheduleService.createTask(request));
    }

    @PostMapping("/schedule/update")
    public ApiResponse<TaskScheduleDTO> update(@RequestBody TaskUpdateRequest request) {
        return ApiResponse.success(scheduleService.updateTask(request.getId(), request));
    }

    @PostMapping("/schedule/delete")
    public ApiResponse<Void> delete(@RequestBody IdRequest request) {
        scheduleService.deleteTask(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/schedule/enable")
    public ApiResponse<Void> enable(@RequestBody IdRequest request) {
        scheduleService.enableTask(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/schedule/disable")
    public ApiResponse<Void> disable(@RequestBody IdRequest request) {
        scheduleService.disableTask(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/schedule/trigger")
    public ApiResponse<Void> trigger(@RequestBody IdRequest request) {
        scheduleService.triggerTask(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/schedule/get")
    public ApiResponse<TaskScheduleDTO> get(@RequestBody IdRequest request) {
        return ApiResponse.success(scheduleService.getTask(request.getId()));
    }

    @PostMapping("/schedule/list")
    public ApiResponse<PageResult<TaskScheduleDTO>> list(@RequestBody TaskQuery query) {
        return ApiResponse.success(scheduleService.listTasks(query));
    }

    @PostMapping("/schedule/logs")
    public ApiResponse<PageResult<TaskExecutionLogDTO>> logs(@RequestBody IdRequest request) {
        return ApiResponse.success(scheduleService.listExecutionLogs(request.getId(), 1, 20));
    }

    @PostMapping("/async/submit")
    public ApiResponse<AsyncTaskDTO> submit(@RequestBody AsyncTaskRequest request) {
        return ApiResponse.success(asyncService.submit(request));
    }

    @PostMapping("/async/get")
    public ApiResponse<AsyncTaskDTO> getAsync(@RequestBody IdRequest request) {
        return ApiResponse.success(asyncService.get(request.getId()));
    }

    @PostMapping("/async/list")
    public ApiResponse<PageResult<AsyncTaskDTO>> listAsync(@RequestBody AsyncTaskQuery query) {
        return ApiResponse.success(asyncService.list(query));
    }

    @PostMapping("/async/cancel")
    public ApiResponse<Void> cancelAsync(@RequestBody IdRequest request) {
        asyncService.cancel(request.getId());
        return ApiResponse.success();
    }
}
