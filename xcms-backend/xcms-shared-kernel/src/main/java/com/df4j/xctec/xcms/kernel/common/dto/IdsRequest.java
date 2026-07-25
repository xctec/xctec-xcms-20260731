package com.df4j.xctec.xcms.kernel.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 通用 ID 列表请求体。
 */
@Data
public class IdsRequest {
    private List<Long> ids;
}
