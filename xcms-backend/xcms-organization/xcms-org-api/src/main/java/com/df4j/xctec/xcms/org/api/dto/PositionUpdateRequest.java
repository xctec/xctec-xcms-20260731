package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

/**
 * 更新岗位请求
 */
@Data
public class PositionUpdateRequest {
    /** 岗位 ID（全 POST 风格，由请求体携带，替代 @PathVariable） */
    private Long id;
    private String positionName;
    private Integer level;
    private Integer sortOrder;
}
