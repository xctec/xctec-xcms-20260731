package com.df4j.xctec.xcms.identity.api;

import com.df4j.xctec.xcms.identity.api.dto.RoleCreateRequest;
import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import com.df4j.xctec.xcms.identity.api.dto.RoleQuery;
import com.df4j.xctec.xcms.identity.api.dto.RoleUpdateRequest;
import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.kernel.common.PageQuery;
import com.df4j.xctec.xcms.kernel.common.PageResult;

import java.util.List;

/**
 * 角色服务
 */
public interface RoleService {

    RoleDTO createRole(RoleCreateRequest request);

    RoleDTO updateRole(Long roleId, RoleUpdateRequest request);

    void deleteRole(Long roleId);

    RoleDTO getRole(Long roleId);

    PageResult<RoleDTO> listRoles(RoleQuery query);

    List<RoleDTO> listByScope(RoleScope scope);

    void assignRoleToUser(Long userId, Long roleId, RoleScope scope, String scopeValue);

    void removeRoleFromUser(Long userId, Long roleId, RoleScope scope, String scopeValue);

    List<RoleDTO> getUserRoles(Long userId);

    PageResult<UserBriefDTO> getRoleUsers(Long roleId, PageQuery query);
}
