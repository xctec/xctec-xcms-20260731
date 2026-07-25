package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

/**
 * 岗位信息
 */
@Data
public class PositionDTO {
    private Long id;
    private Long deptId;
    private String positionCode;
    private String positionName;
    private Integer level;
    private Integer sortOrder;
}
