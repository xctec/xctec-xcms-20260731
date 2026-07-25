package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.tenant.api.enums.QuotaType;
import lombok.Data;

/**
 * 单条配额使用明细
 */
@Data
public class QuotaUsageItem {
    private QuotaType quotaType;
    private Long limit;
    private Long used;
    private Double usagePercentage;
}
