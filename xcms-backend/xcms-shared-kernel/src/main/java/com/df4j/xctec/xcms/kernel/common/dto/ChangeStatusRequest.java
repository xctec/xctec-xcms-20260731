package com.df4j.xctec.xcms.kernel.common.dto;

import lombok.Data;

/**
 * 通用状态变更请求体（id + 状态枚举名/编码的字符串形式）。
 */
@Data
public class ChangeStatusRequest {
    private Long id;
    private String status;
}
