package com.df4j.xctec.xcms.operation.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogQuery extends PageQuery {
    private Long operatorId;
    private String module;
    private String result;
    private LocalDateTime from;
    private LocalDateTime to;
}
