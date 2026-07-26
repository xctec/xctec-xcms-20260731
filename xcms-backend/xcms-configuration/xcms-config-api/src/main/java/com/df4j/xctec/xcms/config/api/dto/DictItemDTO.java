package com.df4j.xctec.xcms.config.api.dto;

import lombok.Data;

@Data
public class DictItemDTO {
    private Long id;
    private Long dictId;
    private String itemCode;
    private String itemValue;
    private Integer sortOrder;
    private Long parentId;
    private String status;
}
