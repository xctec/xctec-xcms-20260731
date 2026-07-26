package com.df4j.xctec.xcms.config.api.dto;

import lombok.Data;

@Data
public class DictTypeDTO {
    private Long id;
    private String dictCode;
    private String dictName;
    private String description;
    private String status;
}
