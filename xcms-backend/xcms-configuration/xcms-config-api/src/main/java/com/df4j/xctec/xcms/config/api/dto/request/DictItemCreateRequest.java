package com.df4j.xctec.xcms.config.api.dto.request;

import lombok.Data;

@Data
public class DictItemCreateRequest {
    private String typeCode;
    private String itemCode;
    private String itemName;
    private String itemValue;
    private Integer sortOrder;
    private String status;
}
