package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

/**
 * 数据规则解绑请求体
 */
@Data
public class DataRuleUnbindRequest {
    private Long id;
    private Long roleId;
}
