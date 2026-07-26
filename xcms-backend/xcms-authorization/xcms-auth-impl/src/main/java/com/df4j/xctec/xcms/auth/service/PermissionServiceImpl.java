package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.auth.api.RolePermissionService;
import com.df4j.xctec.xcms.auth.api.dto.BusinessVisibilityAuth;
import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import com.df4j.xctec.xcms.auth.api.dto.PermissionDTO;
import com.df4j.xctec.xcms.auth.api.enums.MenuScope;
import com.df4j.xctec.xcms.auth.domain.Menu;
import com.df4j.xctec.xcms.auth.domain.Permission;
import com.df4j.xctec.xcms.auth.domain.RolePermission;
import com.df4j.xctec.xcms.auth.repository.CrossTenantAuthRepository;
import com.df4j.xctec.xcms.auth.repository.MenuRepository;
import com.df4j.xctec.xcms.auth.repository.PermissionRepository;
import com.df4j.xctec.xcms.auth.repository.RolePermissionRepository;
import com.df4j.xctec.xcms.identity.api.RoleService;
import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;
    private final CrossTenantAuthRepository crossTenantAuthRepository;

    /**
     * 自注入：让 checkPermission/requirePermission 内部调用 getUserPermissions 时走 Spring AOP 代理，
     * 使 @Cacheable 生效。若直接用 this. 调用（self-invocation），代理被绕过，缓存不会触发，
     * 导致每次鉴权仍全量查 DB（N+1）。@Lazy 避免启动期循环依赖检查。
     */
    private final PermissionService self;

    public PermissionServiceImpl(
            RoleService roleService,
            RolePermissionService rolePermissionService,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository,
            MenuRepository menuRepository,
            CrossTenantAuthRepository crossTenantAuthRepository,
            @Lazy PermissionService self) {
        this.roleService = roleService;
        this.rolePermissionService = rolePermissionService;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.menuRepository = menuRepository;
        this.crossTenantAuthRepository = crossTenantAuthRepository;
        this.self = self;
    }

    @Override
    @Cacheable(cacheNames = "userPermissions", key = "#userId", unless = "#result.isEmpty()")
    public Set<String> getUserPermissions(Long userId) {
        List<RoleDTO> roles = roleService.getUserRoles(userId);
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        List<Long> roleIds = roles.stream().map(RoleDTO::getId).toList();
        List<Long> permIds = rolePermissionRepository.findByRoleIdIn(roleIds).stream()
                .map(rp -> rp.getPermissionId()).toList();
        if (permIds.isEmpty()) {
            return Set.of();
        }
        Set<String> result = permissionRepository.findByIdIn(permIds).stream()
                .map(Permission::getPermCode).collect(Collectors.toSet());
        // 返回不可变 Set，防止调用方修改污染缓存中的引用
        return Collections.unmodifiableSet(result);
    }

    @Override
    public boolean checkPermission(Long userId, String permCode) {
        // 通过 self 调用走代理，使 @Cacheable 生效
        return self.getUserPermissions(userId).contains(permCode);
    }

    @Override
    public void requirePermission(Long userId, String permCode) {
        if (!checkPermission(userId, permCode)) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, permCode);
        }
    }

    @Override
    public List<MenuDTO> getUserMenus(Long userId, MenuScope scope) {
        boolean isAdmin = roleService.getUserRoles(userId).stream()
                .anyMatch(r -> "tenant_admin".equals(r.getRoleCode()));
        List<Menu> all = menuRepository.findAll();
        String scopeName = scope == null ? null : scope.name();
        Set<Long> allowedMenuIds = getUserMenuIds(userId);
        List<Menu> visible = all.stream().filter(m -> {
            if (m.getVisible() != null && !m.getVisible()) {
                return false;
            }
            if (isAdmin || MenuScope.BOTH == scope) {
                return true;
            }
            String ms = m.getScope();
            if (ms == null || "BOTH".equals(ms)) {
                return true;
            }
            return scopeName != null && ms.equals(scopeName);
        }).filter(m -> isAdmin || allowedMenuIds.contains(m.getId())).toList();
        return buildTree(visible);
    }

    @Override
    public List<PermissionDTO> listAllPermissions() {
        List<PermissionDTO> result = new ArrayList<>();
        // 操作类权限：来自 perm_operation
        for (Permission p : permissionRepository.findAll()) {
            PermissionDTO dto = new PermissionDTO();
            dto.setId(p.getId());
            dto.setPermCode(p.getPermCode());
            dto.setPermName(p.getPermName());
            dto.setPermType("OPERATION");
            dto.setModule(p.getModule());
            dto.setAction(p.getAction());
            result.add(dto);
        }
        // 菜单/按钮类权限：来自 perm_menu，使用 menu.permission 作为权限码
        for (Menu m : menuRepository.findAll()) {
            if (m.getPermission() == null || m.getPermission().isBlank()) {
                continue;
            }
            PermissionDTO dto = new PermissionDTO();
            dto.setId(m.getId());
            dto.setPermCode(m.getPermission());
            dto.setPermName(m.getMenuName());
            dto.setPermType("MENU");
            result.add(dto);
        }
        return result;
    }

    /**
     * 计算当前用户通过「MENU」类型角色权限可访问的菜单 id 集合。
     */
    private Set<Long> getUserMenuIds(Long userId) {
        List<RoleDTO> roles = roleService.getUserRoles(userId);
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        List<Long> roleIds = roles.stream().map(RoleDTO::getId).toList();
        return rolePermissionRepository.findByRoleIdIn(roleIds).stream()
                .filter(rp -> "MENU".equals(rp.getPermType()))
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    @Override
    public BusinessVisibilityAuth checkBusinessVisibility(Long userId, Long targetTenantId) {
        List<com.df4j.xctec.xcms.auth.domain.CrossTenantAuth> list =
                crossTenantAuthRepository.findByUserIdAndTargetTenantIdAndStatus(userId, targetTenantId, "ACTIVE");
        if (list.isEmpty()) {
            return null;
        }
        com.df4j.xctec.xcms.auth.domain.CrossTenantAuth a = list.get(0);
        BusinessVisibilityAuth bva = new BusinessVisibilityAuth();
        bva.setToken(a.getToken());
        bva.setTargetTenantId(a.getTargetTenantId());
        bva.setDataScope(a.getDataScope());
        bva.setValidUntil(a.getValidUntil());
        return bva;
    }

    private List<MenuDTO> buildTree(List<Menu> menus) {
        Map<Long, MenuDTO> map = new LinkedHashMap<>();
        for (Menu m : menus) {
            MenuDTO dto = new MenuDTO();
            dto.setId(m.getId());
            dto.setMenuCode(m.getMenuCode());
        dto.setPermission(m.getPermission());
            dto.setMenuName(m.getMenuName());
            dto.setMenuType(m.getMenuType());
            dto.setPath(m.getPath());
            dto.setIcon(m.getIcon());
            dto.setSortOrder(m.getSortOrder());
            dto.setChildren(new ArrayList<>());
            map.put(m.getId(), dto);
        }
        List<MenuDTO> roots = new ArrayList<>();
        for (Menu m : menus) {
            MenuDTO dto = map.get(m.getId());
            if (m.getParentId() != null && map.containsKey(m.getParentId())) {
                map.get(m.getParentId()).getChildren().add(dto);
            } else {
                roots.add(dto);
            }
        }
        return roots;
    }
}
