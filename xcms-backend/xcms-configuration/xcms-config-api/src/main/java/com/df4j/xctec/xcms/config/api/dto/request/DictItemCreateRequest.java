package com.df4j.xctec.xcms.config.api.dto.request;

import lombok.Data;

@Data
public class DictItemCreateRequest {
    private String dictCode;
    private String itemCode;
    private String itemValue;
    private Integer sortOrder;
    private Long parentId;
    private String status;
}
