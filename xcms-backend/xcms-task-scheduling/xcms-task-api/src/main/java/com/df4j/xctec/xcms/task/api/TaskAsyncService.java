package com.df4j.xctec.xcms.task.api;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.task.api.dto.AsyncTaskDTO;
import com.df4j.xctec.xcms.task.api.dto.request.AsyncTaskQuery;
import com.df4j.xctec.xcms.task.api.dto.request.AsyncTaskRequest;

/**
 * 异步任务（后台队列）管理。
 */
public interface TaskAsyncService {

    AsyncTaskDTO submit(AsyncTaskRequest request);

    AsyncTaskDTO get(Long id);

    PageResult<AsyncTaskDTO> list(AsyncTaskQuery query);

    /** 取消（仅 PENDING 可取消） */
    void cancel(Long id);
}
