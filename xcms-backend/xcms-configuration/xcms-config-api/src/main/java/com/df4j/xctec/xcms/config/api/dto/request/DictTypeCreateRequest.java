package com.df4j.xctec.xcms.config.api.dto.request;

import lombok.Data;

@Data
public class DictTypeCreateRequest {
    private String typeCode;
    private String typeName;
    private String remark;
    private String status;
}
