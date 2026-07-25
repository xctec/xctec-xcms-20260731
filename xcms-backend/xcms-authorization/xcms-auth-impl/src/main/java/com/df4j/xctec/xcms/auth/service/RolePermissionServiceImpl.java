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
import java.util.List;
import java.util.Map;
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
        for (PermissionAssignRequest req : permissions) {
            if (rolePermissionRepository.findByRoleIdAndPermissionId(roleId, req.getPermId()).isEmpty()) {
                rolePermissionRepository.save(RolePermission.builder()
                        .roleId(roleId).permissionId(req.getPermId())
                        .permType(req.getPermType()).scopeConfig(req.getScopeConfig()).build());
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
