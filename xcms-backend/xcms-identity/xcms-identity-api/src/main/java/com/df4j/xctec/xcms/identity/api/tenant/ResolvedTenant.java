package com.df4j.xctec.xcms.identity.api.tenant;

/**
 * 解析出的租户/用户上下文。
 *
 * @param tenantId 租户 ID（必填）
 * @param userId   用户 ID（可选，匿名/未登录场景可能为 null）
 */
public record ResolvedTenant(Long tenantId, Long userId) {
}
