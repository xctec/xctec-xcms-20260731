package com.df4j.xctec.xcms.operation.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AlertRuleQuery extends PageQuery {
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "指标Key")
    private String metricKey;
}
