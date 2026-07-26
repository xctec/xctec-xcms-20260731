package com.df4j.xctec.xcms.operation.api.dto.request;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogQuery extends PageQuery {
    @Schema(description = "操作人ID")
    private Long operatorId;
    @Schema(description = "操作模块")
    private String module;
    @Schema(description = "执行结果")
    private String result;
    @Schema(description = "开始时间")
    private LocalDateTime from;
    @Schema(description = "结束时间")
    private LocalDateTime to;
}
