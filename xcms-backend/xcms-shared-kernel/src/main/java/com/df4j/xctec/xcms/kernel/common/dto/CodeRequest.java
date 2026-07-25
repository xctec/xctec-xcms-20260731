package com.df4j.xctec.xcms.kernel.common.dto;

import lombok.Data;

/**
 * 通用编码/名称请求体（用于按 code / username 等查询）。
 */
@Data
public class CodeRequest {
    private String code;
}
