package com.df4j.xctec.xcms.task.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AsyncTaskQuery extends PageQuery {
    private Long tenantId;
    private String taskType;
    private String status;
}
