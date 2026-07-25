package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

/**
 * 数据规则绑定角色请求体
 */
@Data
public class DataRuleBindRequest {
    private Long id;
    private Long roleId;
    private String scopeValue;
}
