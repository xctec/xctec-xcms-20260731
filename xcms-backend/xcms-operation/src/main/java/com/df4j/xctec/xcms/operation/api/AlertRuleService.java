package com.df4j.xctec.xcms.operation.api;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.operation.api.dto.AlertRecordDTO;
import com.df4j.xctec.xcms.operation.api.dto.AlertRuleDTO;
import com.df4j.xctec.xcms.operation.api.dto.request.AlertRuleCreateRequest;
import com.df4j.xctec.xcms.operation.api.dto.request.AlertRuleQuery;
import com.df4j.xctec.xcms.operation.api.dto.request.AlertRuleUpdateRequest;

/**
 * 告警规则服务。
 */
public interface AlertRuleService {

    AlertRuleDTO create(AlertRuleCreateRequest request);

    AlertRuleDTO update(AlertRuleUpdateRequest request);

    void delete(Long id);

    void enable(Long id);

    void disable(Long id);

    AlertRuleDTO get(Long id);

    PageResult<AlertRuleDTO> list(AlertRuleQuery query);

    /** 评估所有启用规则并触发告警（由调度器周期调用） */
    void evaluate();

    PageResult<AlertRecordDTO> records(int page, int size);
}
