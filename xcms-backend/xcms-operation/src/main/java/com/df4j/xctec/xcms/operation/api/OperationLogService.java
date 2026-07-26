package com.df4j.xctec.xcms.operation.api;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.operation.api.dto.OperationLogDTO;
import com.df4j.xctec.xcms.operation.api.dto.request.OperationLogQuery;

/**
 * 操作日志服务。
 */
public interface OperationLogService {

    void record(String module, String action, String bizType, String bizId,
                String detail, boolean success, String errorMsg, Long durationMs);

    OperationLogDTO get(Long id);

    PageResult<OperationLogDTO> list(OperationLogQuery query);
}
