package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

/**
 * 数据规则权限测试请求体
 */
@Data
public class DataRuleTestRequest {
    private Long userId;
    private String resourceType;
}
