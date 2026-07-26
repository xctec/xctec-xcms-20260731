package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.api.RolePermissionService;
import com.df4j.xctec.xcms.auth.api.dto.PermissionAssignRequest;
import com.df4j.xctec.xcms.auth.api.dto.PermissionDTO;
import com.df4j.xctec.xcms.auth.api.event.PermissionChangedEvent;
import com.df4j.xctec.xcms.auth.domain.Menu;
import com.df4j.xctec.xcms.auth.domain.Permission;
import com.df4j.xctec.xcms.auth.domain.RolePermission;
import com.df4j.xctec.xcms.auth.repository.MenuRepository;
import com.df4j.xctec.xcms.auth.repository.PermissionRepository;
import com.df4j.xctec.xcms.auth.repository.RolePermissionRepository;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, List<PermissionAssignRequest> permissions) {
        // 按 permId 批量查 Permission 表(perm_operation)与 Menu 表(perm_menu)，自动推断 permType：
        // 命中 Permission 表记为 OPERATION，命中 Menu 表记为 MENU。
        // 前端未传 permType 时使用推断值，避免 permType=null 导致 getRolePermissions
        // 按 permType 分组查询时漏掉记录（分配成功但刷新查不到）。
        List<Long> permIds = permissions.stream().map(PermissionAssignRequest::getPermId).toList();
        Set<Long> operationIds = permIds.isEmpty() ? new HashSet<>()
                : permissionRepository.findByIdIn(permIds).stream()
                        .map(Permission::getId).collect(Collectors.toSet());
        Set<Long> menuIds = permIds.isEmpty() ? new HashSet<>()
                : menuRepository.findByIdIn(permIds).stream()
                        .map(Menu::getId).collect(Collectors.toSet());
        for (PermissionAssignRequest req : permissions) {
            String permType = req.getPermType();
            if (permType == null || permType.isBlank()) {
                if (operationIds.contains(req.getPermId())) {
                    permType = "OPERATION";
                } else if (menuIds.contains(req.getPermId())) {
                    permType = "MENU";
                } else {
                    permType = "OPERATION";
                }
            }
            String finalPermType = permType;
            if (rolePermissionRepository.findByRoleIdAndPermissionId(roleId, req.getPermId()).isEmpty()) {
                rolePermissionRepository.save(RolePermission.builder()
                        .roleId(roleId).permissionId(req.getPermId())
                        .permType(finalPermType).scopeConfig(req.getScopeConfig()).build());
            }
        }
        PermissionChangedEvent assignEvent = new PermissionChangedEvent();
        assignEvent.setUserId(roleId);
        assignEvent.setTenantId(TenantContext.getTenantId());
        assignEvent.setChangeType("ASSIGN");
        eventPublisher.publish(assignEvent);
    }

    @Override
    @Transactional
    public void removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        for (Long permId : permissionIds) {
            rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permId);
        }
        PermissionChangedEvent removeEvent = new PermissionChangedEvent();
        removeEvent.setUserId(roleId);
        removeEvent.setTenantId(TenantContext.getTenantId());
        removeEvent.setChangeType("REMOVE");
        eventPublisher.publish(removeEvent);
    }

    @Override
    public List<PermissionDTO> getRolePermissions(Long roleId) {
        List<RolePermission> rps = rolePermissionRepository.findByRoleId(roleId);
        if (rps.isEmpty()) {
            return List.of();
        }
        Map<Long, String> typeMap = rps.stream()
                .collect(Collectors.toMap(RolePermission::getPermissionId, RolePermission::getPermType, (a, b) -> a));
        List<PermissionDTO> result = new ArrayList<>();
        List<Long> opIds = rps.stream()
                .filter(rp -> "OPERATION".equals(rp.getPermType()))
                .map(RolePermission::getPermissionId).toList();
        for (Permission p : permissionRepository.findByIdIn(opIds)) {
            PermissionDTO dto = new PermissionDTO();
            dto.setId(p.getId());
            dto.setPermCode(p.getPermCode());
            dto.setPermName(p.getPermName());
            dto.setPermType(typeMap.get(p.getId()));
            dto.setModule(p.getModule());
            dto.setAction(p.getAction());
            result.add(dto);
        }
        List<Long> menuIds = rps.stream()
                .filter(rp -> "MENU".equals(rp.getPermType()))
                .map(RolePermission::getPermissionId).toList();
        for (Menu m : menuRepository.findByIdIn(menuIds)) {
            PermissionDTO dto = new PermissionDTO();
            dto.setId(m.getId());
            dto.setPermCode(m.getMenuCode());
            dto.setPermName(m.getMenuName());
            dto.setPermType("MENU");
            result.add(dto);
        }
        return result;
    }
}
