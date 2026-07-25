package com.df4j.xctec.xcms.kernel.common.dto;

import lombok.Data;

/**
 * 通用单 ID 请求体（全 POST 风格，替代 @PathVariable / @RequestParam）。
 */
@Data
public class IdRequest {
    private Long id;
}
