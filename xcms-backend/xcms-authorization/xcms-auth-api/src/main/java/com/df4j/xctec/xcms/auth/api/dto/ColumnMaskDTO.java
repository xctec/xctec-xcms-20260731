package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ColumnMaskDTO {
    private Long id;
    private String resourceType;
    private String fieldName;
    private String maskType;
    private String maskRule;
    private List<Long> roleIds;
}
