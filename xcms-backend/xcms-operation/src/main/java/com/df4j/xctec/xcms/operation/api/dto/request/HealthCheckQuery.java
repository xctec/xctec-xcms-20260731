package com.df4j.xctec.xcms.operation.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HealthCheckQuery extends PageQuery {
    @Schema(description = "健康检查状态")
    private String status;
    @Schema(description = "检查类型")
    private String checkType;
}
