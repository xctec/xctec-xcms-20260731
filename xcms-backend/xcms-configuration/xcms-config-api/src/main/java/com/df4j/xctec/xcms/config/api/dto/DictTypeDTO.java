package com.df4j.xctec.xcms.config.api.dto;

import lombok.Data;

@Data
public class DictTypeDTO {
    private Long id;
    private String typeCode;
    private String typeName;
    private String remark;
    private String status;
}
