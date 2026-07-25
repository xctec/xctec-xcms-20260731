package com.df4j.xctec.xcms.tenant.api.dto;

import com.df4j.xctec.xcms.tenant.api.enums.QuotaType;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 配额分配请求
 */
@Data
public class QuotaAllocateRequest {
    /** 配额类型 */
    @NotNull
    private QuotaType quotaType;
    /** 配额上限 */
    @NotNull
    private Long quotaLimit;
    /** 周期：DAILY / MONTHLY / TOTAL */
    @NotNull
    private String period;
}
