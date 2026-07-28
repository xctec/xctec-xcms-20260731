package com.df4j.xctec.xcms.datapermission.api;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * 数据权限门面（ADR-013 / AT-16）。
 *
 * <p>从 auth 模块下沉至独立的 data-permission 模块：业务服务只依赖本 api
 * 即获得行级过滤 + 列级脱敏能力，不再为数据权限依赖 auth-impl。
 * 规则来源经 {@link DataPermissionRuleProvider} / {@link ColumnMaskRuleProvider}
 * SPI 汇总（auth-impl 作为"管理面提供者"之一）。</p>
 */
public interface DataPermissionService {

    /** 行级数据范围 Specification，包装业务查询即生效 */
    <T> Specification<T> getDataScopeSpec(Long userId, String resourceType);

    /** 解析用户在指定资源上的数据权限上下文（各维度范围聚合） */
    DataPermissionContext getDataPermissionContext(Long userId, String resourceType);

    /** 列级脱敏（单对象，须为 DTO，禁止 JPA 实体） */
    <T> T applyColumnMask(T entity, Long userId, String resourceType);

    /** 列级脱敏（列表） */
    <T> List<T> applyColumnMask(List<T> entities, Long userId, String resourceType);
}
