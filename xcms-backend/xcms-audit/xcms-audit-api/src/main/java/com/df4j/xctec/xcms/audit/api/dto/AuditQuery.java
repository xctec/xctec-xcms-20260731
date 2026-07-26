package com.df4j.xctec.xcms.audit.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuditQuery {
    @Schema(description = "事件唯一标识")
    private String eventId;
    @Schema(description = "业务模块标识")
    private String bizModule;
    @Schema(description = "事件类型")
    private String eventType;
    @Schema(description = "操作人ID")
    private Long operatorId;
    @Schema(description = "是否成功")
    private Boolean success;
    @Schema(description = "业务对象ID")
    private String bizId;
    @Schema(description = "页码，从 1 开始", example = "1")
    private int page = 1;
    @Schema(description = "每页条数", example = "20")
    private int size = 20;
}
