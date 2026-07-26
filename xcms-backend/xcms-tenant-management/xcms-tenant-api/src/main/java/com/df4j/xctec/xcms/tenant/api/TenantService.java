package com.df4j.xctec.xcms.tenant.api;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.tenant.api.dto.TenantCreateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantLookupDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuery;
import com.df4j.xctec.xcms.tenant.api.dto.TenantTreeDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantUpdateRequest;
import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;

import java.util.List;

/**
 * 租户管理服务
 */
public interface TenantService {

    /**
     * 创建下级租户
     *
     * @param request 创建请求
     * @return 租户信息
     */
    TenantDTO createTenant(TenantCreateRequest request);

    /**
     * 更新租户信息
     */
    TenantDTO updateTenant(Long tenantId, TenantUpdateRequest request);

    /**
     * 获取租户详情
     */
    TenantDTO getTenantById(Long tenantId);

    /**
     * 根据编码获取租户
     */
    TenantDTO getTenantByCode(String tenantCode);

    /**
     * 获取租户树（当前租户的子树）
     */
    List<TenantTreeDTO> getTenantTree(Long rootTenantId);

    /**
     * 获取子租户列表
     */
    PageResult<TenantDTO> listSubTenants(Long parentId, TenantQuery query);

    /**
     * 启用/停用/锁定租户
     */
    void changeTenantStatus(Long tenantId, TenantStatus status);

    /**
     * 迁移租户到新的父租户
     */
    void migrateTenant(Long tenantId, Long newParentId);

    /**
     * 获取租户路径上的所有祖先租户（含自身）
     */
    List<TenantDTO> getTenantAncestors(Long tenantId);

    /**
     * 检查是否为祖先租户（用于权限判断）
     */
    boolean isAncestor(Long ancestorTenantId, Long descendantTenantId);

    /**
     * 查找启用状态的租户（免鉴权，登录页选择租户用，仅返回精简信息）。
     *
     * @param keyword 关键字（模糊匹配编码/名称），为空返回全部启用租户
     * @return 精简租户列表（id/code/name），最多 50 条
     */
    List<TenantLookupDTO> lookupActiveTenants(String keyword);
}
