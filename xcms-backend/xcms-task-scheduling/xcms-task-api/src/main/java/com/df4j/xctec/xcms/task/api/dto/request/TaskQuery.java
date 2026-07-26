package com.df4j.xctec.xcms.task.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQuery extends PageQuery {
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "任务类型")
    private String taskType;
    @Schema(description = "状态（ENABLED/DISABLED/PAUSED）")
    private String status;
    @Schema(description = "关键字（任务名/编码模糊匹配）")
    private String keyword;
}
