package com.df4j.xctec.xcms.operation.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class MetricQuery extends PageQuery {
    private String metricKey;
    private LocalDateTime from;
    private LocalDateTime to;
}
