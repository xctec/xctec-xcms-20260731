package com.df4j.xctec.xcms.kernel.common.dto;

import lombok.Data;

/**
 * 通用移动/变更归属请求体（id + 目标父级/目标节点）。
 */
@Data
public class MoveRequest {
    private Long id;
    private Long targetId;
}
