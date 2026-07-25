package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

/**
 * 创建岗位请求
 */
@Data
public class PositionCreateRequest {
    private Long deptId;
    private String positionCode;
    private String positionName;
    private Integer level;
    private Integer sortOrder;
}
