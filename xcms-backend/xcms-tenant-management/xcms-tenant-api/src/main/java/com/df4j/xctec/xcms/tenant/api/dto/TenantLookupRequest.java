package com.df4j.xctec.xcms.tenant.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户查找请求（免鉴权，登录页选择租户用）
 */
@Data
public class TenantLookupRequest {

    @Schema(description = "关键字（模糊匹配租户编码/名称），为空则返回全部启用租户")
    private String keyword;
}
