package com.df4j.xctec.xcms.config.api.dto.request;

import lombok.Data;

@Data
public class DictTypeCreateRequest {
    private String dictCode;
    private String dictName;
    private String description;
    private String status;
}
