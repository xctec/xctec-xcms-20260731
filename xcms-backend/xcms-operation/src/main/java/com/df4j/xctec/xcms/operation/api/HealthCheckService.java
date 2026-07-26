package com.df4j.xctec.xcms.operation.api;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.operation.api.dto.HealthCheckDTO;
import com.df4j.xctec.xcms.operation.api.dto.HealthCheckLogDTO;
import com.df4j.xctec.xcms.operation.api.dto.request.HealthCheckCreateRequest;
import com.df4j.xctec.xcms.operation.api.dto.request.HealthCheckQuery;
import com.df4j.xctec.xcms.operation.api.dto.request.HealthCheckUpdateRequest;

/**
 * 健康检查服务。
 */
public interface HealthCheckService {

    HealthCheckDTO create(HealthCheckCreateRequest request);

    HealthCheckDTO update(HealthCheckUpdateRequest request);

    void delete(Long id);

    void enable(Long id);

    void disable(Long id);

    void run(Long id);

    HealthCheckDTO get(Long id);

    PageResult<HealthCheckDTO> list(HealthCheckQuery query);

    PageResult<HealthCheckLogDTO> logs(Long checkId, int page, int size);
}
