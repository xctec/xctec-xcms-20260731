package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

/**
 * 用户岗位关联
 */
@Data
public class UserPositionDTO {
    private Long id;
    private Long userId;
    private Long deptId;
    private String deptName;
    private Long positionId;
    private String positionName;
    private Boolean isPrimary;
}
