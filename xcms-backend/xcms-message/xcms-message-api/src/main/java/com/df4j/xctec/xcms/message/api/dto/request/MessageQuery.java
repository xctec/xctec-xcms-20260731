package com.df4j.xctec.xcms.message.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MessageQuery {
    @Schema(description = "页码，从 1 开始", example = "1")
    private Integer page = 1;
    @Schema(description = "每页条数", example = "20")
    private Integer size = 20;
}
