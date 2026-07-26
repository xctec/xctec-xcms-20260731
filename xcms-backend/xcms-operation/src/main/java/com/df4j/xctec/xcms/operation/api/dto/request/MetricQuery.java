package com.df4j.xctec.xcms.operation.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class MetricQuery extends PageQuery {
    @Schema(description = "指标Key")
    private String metricKey;
    @Schema(description = "统计开始时间")
    private LocalDateTime from;
    @Schema(description = "统计结束时间")
    private LocalDateTime to;
}
