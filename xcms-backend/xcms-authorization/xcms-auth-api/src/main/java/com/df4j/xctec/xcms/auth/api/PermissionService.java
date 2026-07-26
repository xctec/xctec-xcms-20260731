package com.df4j.xctec.xcms.auth.api;

import com.df4j.xctec.xcms.auth.api.dto.BusinessVisibilityAuth;
import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import com.df4j.xctec.xcms.auth.api.enums.MenuScope;

import java.util.List;
import java.util.Set;

public interface PermissionService {

    boolean checkPermission(Long userId, String permCode);

    void requirePermission(Long userId, String permCode);

    Set<String> getUserPermissions(Long userId);

    List<MenuDTO> getUserMenus(Long userId, MenuScope scope);

    /** 列出全部权限（OPERATION 操作权限 + MENU 菜单/按钮权限），供前端分配权限时全量选择 */
    List<PermissionDTO> listAllPermissions();

    BusinessVisibilityAuth checkBusinessVisibility(Long userId, Long targetTenantId);
}
