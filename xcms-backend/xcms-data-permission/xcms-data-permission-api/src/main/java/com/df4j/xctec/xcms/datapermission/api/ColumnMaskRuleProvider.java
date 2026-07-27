package com.df4j.xctec.xcms.datapermission.api;

import java.util.List;

/**
 * 列级脱敏规则提供者 SPI（ADR-013 / AT-16）。
 *
 * <p>auth-impl 作为管理面提供者从 {@code perm_column_mask} 提供规则；
 * 业务模块也可注册自定义 Provider。</p>
 */
public interface ColumnMaskRuleProvider {

    /**
     * 返回指定资源类型的生效脱敏规则。
     *
     * @param resourceType 资源类型
     * @param tenantId     租户 ID
     * @return 脱敏规则列表，无规则返回空列表（不得返回 null）
     */
    List<ColumnMaskSpec> getMasks(String resourceType, Long tenantId);
}
