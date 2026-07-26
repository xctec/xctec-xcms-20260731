package com.df4j.xctec.xcms.operation.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HealthCheckQuery extends PageQuery {
    private String status;
    private String checkType;
}
