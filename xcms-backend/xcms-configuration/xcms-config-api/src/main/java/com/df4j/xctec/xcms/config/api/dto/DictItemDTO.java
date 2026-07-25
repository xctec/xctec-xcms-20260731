package com.df4j.xctec.xcms.config.api.dto;

import lombok.Data;

@Data
public class DictItemDTO {
    private Long id;
    private String typeCode;
    private String itemCode;
    private String itemName;
    private String itemValue;
    private Integer sortOrder;
    private String status;
}
