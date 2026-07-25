package com.df4j.xctec.xcms.tenant.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 配额使用情况
 */
@Data
public class QuotaUsageDTO {
    private Long tenantId;
    private List<QuotaUsageItem> items;
}
