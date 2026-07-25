package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ColumnMaskUpdateRequest {
    /** 规则 ID（全 POST 风格，由请求体携带，替代 @PathVariable） */
    private Long id;
    private String resourceType;
    private String fieldName;
    private String maskType;
    private String maskRule;
    private List<Long> roleIds;
}
