package com.df4j.xctec.xcms.task.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AsyncTaskQuery extends PageQuery {
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "任务类型")
    private String taskType;
    @Schema(description = "状态（PENDING/RUNNING/SUCCESS/FAILED）")
    private String status;
}
