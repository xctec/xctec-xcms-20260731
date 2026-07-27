package com.df4j.xctec.xcms.datapermission.api;

import org.springframework.data.jpa.domain.Specification;

/**
 * 业务查询数据范围包装基类（ADR-013 / AT-17）。
 *
 * <p>业务 Service 继承本类（或直接注入 {@link DataPermissionService}），
 * 用 {@link #scoped} 包装 Specification 查询即获得行级过滤：</p>
 *
 * <pre>{@code
 * @Service
 * public class OrderQueryService extends DataScopedRepositorySupport {
 *     public OrderQueryService(DataPermissionService dps) { super(dps); }
 *
 *     public Page<Order> page(Specification<Order> cond, Long userId, Pageable p) {
 *         return orderRepository.findAll(scoped("order", userId, cond), p);
 *     }
 * }
 * }</pre>
 */
public abstract class DataScopedRepositorySupport {

    protected final DataPermissionService dataPermissionService;

    protected DataScopedRepositorySupport(DataPermissionService dataPermissionService) {
        this.dataPermissionService = dataPermissionService;
    }

    /** 仅数据范围过滤 */
    protected <T> Specification<T> scoped(String resourceType, Long userId) {
        return dataPermissionService.getDataScopeSpec(userId, resourceType);
    }

    /** 业务条件 AND 数据范围过滤（business 为 null 时等价于仅数据范围） */
    protected <T> Specification<T> scoped(String resourceType, Long userId, Specification<T> business) {
        Specification<T> scope = dataPermissionService.getDataScopeSpec(userId, resourceType);
        return business == null ? scope : scope.and(business);
    }
}
