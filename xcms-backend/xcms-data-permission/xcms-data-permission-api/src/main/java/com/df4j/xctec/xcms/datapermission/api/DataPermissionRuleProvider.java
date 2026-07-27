package com.df4j.xctec.xcms.datapermission.api;

import java.util.List;

/**
 * 行级数据权限规则提供者 SPI（ADR-013 / AT-16）。
 *
 * <p>各业务模块可自行注册 Provider 提供自己的规则来源；auth-impl 作为
 * "管理面提供者"，从 {@code perm_data_rule} 按用户角色解析规则。
 * {@code DataPermissionService} 汇总所有 Provider 的规则生成数据范围。</p>
 *
 * <p>注：规则通常绑定角色，需按用户维度解析，故 SPI 相比任务书增加
 * {@code userId} 参数（"用户→角色→规则"的解析职责在 Provider 侧完成，
 * 使 data-permission 模块无需依赖 identity）。</p>
 */
public interface DataPermissionRuleProvider {

    /**
     * 返回用户在指定资源类型上生效的数据规则。
     *
     * @param resourceType 资源类型（如 user/order）
     * @param tenantId     租户 ID
     * @param userId       用户 ID
     * @return 规则列表，无规则返回空列表（不得返回 null）
     */
    List<DataRuleSpec> getRules(String resourceType, Long tenantId, Long userId);
}
