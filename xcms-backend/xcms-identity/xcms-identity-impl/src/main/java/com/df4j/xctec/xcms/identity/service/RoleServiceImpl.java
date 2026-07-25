package com.df4j.xctec.xcms.identity.service;

import com.df4j.xctec.xcms.identity.api.RoleService;
import com.df4j.xctec.xcms.identity.api.dto.RoleCreateRequest;
import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import com.df4j.xctec.xcms.identity.api.dto.RoleQuery;
import com.df4j.xctec.xcms.identity.api.dto.RoleUpdateRequest;
import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.identity.api.event.RoleAssignedEvent;
import com.df4j.xctec.xcms.identity.domain.Role;
import com.df4j.xctec.xcms.identity.domain.User;
import com.df4j.xctec.xcms.identity.domain.UserRole;
import com.df4j.xctec.xcms.identity.mapper.RoleMapper;
import com.df4j.xctec.xcms.identity.repository.RoleRepository;
import com.df4j.xctec.xcms.identity.repository.UserRepository;
import com.df4j.xctec.xcms.identity.repository.UserRoleRepository;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.kernel.common.PageQuery;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMapper roleMapper;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public RoleDTO createRole(RoleCreateRequest request) {
        requireTenant();
        if (roleRepository.existsByRoleCode(request.getRoleCode())) {
            throw new BusinessException(ErrorCodes.ROLE_ALREADY_EXISTS, "角色编码已存在: " + request.getRoleCode());
        }
        Role role = Role.builder()
                .roleCode(request.getRoleCode())
                .roleName(request.getRoleName())
                .roleType(request.getRoleType() != null ? request.getRoleType() : RoleScope.TENANT.name())
                .description(request.getDescription())
                .build();
        return roleMapper.toDTO(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleDTO updateRole(Long roleId, RoleUpdateRequest request) {
        requireTenant();
        Role role = getById(roleId);
        if (request.getRoleName() != null) role.setRoleName(request.getRoleName());
        if (request.getDescription() != null) role.setDescription(request.getDescription());
        return roleMapper.toDTO(roleRepository.save(role));
    }

    @Override
    public RoleDTO getRole(Long roleId) {
        requireTenant();
        return roleMapper.toDTO(getById(roleId));
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        requireTenant();
        Role role = getById(roleId);
        role.setDeletedAt(LocalDateTime.now());
        roleRepository.save(role);
    }

    @Override
    public PageResult<RoleDTO> listRoles(RoleQuery query) {
        requireTenant();
        org.springframework.data.domain.Pageable pageable = PageRequest.of(
                Math.max(query.getPage() - 1, 0), Math.max(query.getSize(), 1),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<Role> roles = roleRepository.search(query.getKeyword(), query.getRoleType(), pageable);
        return PageResult.of(roleMapper.toDTOList(roles.getContent()), roles.getTotalElements());
    }

    @Override
    public List<RoleDTO> listByScope(RoleScope scope) {
        List<Role> roles = roleRepository.findByRoleScope(scope);
        return roleMapper.toDTOList(roles);
    }

    @Override
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId, RoleScope scope, String scopeValue) {
        requireTenant();
        String scopeType = scope != null ? scope.name() : null;
        if (userRoleRepository.findByUserIdAndRoleIdAndScopeTypeAndScopeValue(userId, roleId, scopeType, scopeValue).isEmpty()) {
            userRoleRepository.save(UserRole.builder()
                    .userId(userId).roleId(roleId)
                    .scopeType(scopeType).scopeValue(scopeValue)
                    .grantedBy(TenantContext.getCurrentUserId())
                    .grantedAt(LocalDateTime.now())
                    .build());
            RoleAssignedEvent evt = new RoleAssignedEvent();
            evt.setUserId(userId);
            evt.setRoleId(roleId);
            evt.setTenantId(TenantContext.getTenantId());
            evt.setScopeType(scopeType);
            evt.setScopeValue(scopeValue);
            eventPublisher.publish(evt);
        }
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId, RoleScope scope, String scopeValue) {
        requireTenant();
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    public List<RoleDTO> getUserRoles(Long userId) {
        List<UserRole> urs = userRoleRepository.findByUserId(userId);
        if (urs.isEmpty()) return List.of();
        List<Role> roles = roleRepository.findAllById(urs.stream().map(UserRole::getRoleId).toList());
        return roleMapper.toDTOList(roles);
    }

    @Override
    public PageResult<UserBriefDTO> getRoleUsers(Long roleId, PageQuery query) {
        List<UserRole> urs = userRoleRepository.findByRoleId(roleId);
        List<Long> userIds = urs.stream().map(UserRole::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return PageResult.of(List.of(), 0);
        }
        List<User> users = userRepository.findAllById(userIds);
        List<UserBriefDTO> list = users.stream().map(u -> {
            UserBriefDTO d = new UserBriefDTO();
            d.setId(u.getId());
            d.setUsername(u.getUsername());
            d.setRealName(u.getRealName());
            return d;
        }).toList();
        return PageResult.of(list, list.size());
    }

    public void grantToUser(Long userId, Long roleId) {
        assignRoleToUser(userId, roleId, RoleScope.TENANT, String.valueOf(TenantContext.getTenantId()));
    }

    public void revokeFromUser(Long userId, Long roleId) {
        removeRoleFromUser(userId, roleId, RoleScope.TENANT, String.valueOf(TenantContext.getTenantId()));
    }

    @Transactional
    public void createDefaultRolesForTenant(Long tenantId) {
        TenantContext.TenantInfo original = TenantContext.switchTo(tenantId);
        try {
            if (roleRepository.findByRoleCode("tenant_admin").isEmpty()) {
                roleRepository.save(Role.builder()
                        .roleCode("tenant_admin")
                        .roleName("租户管理员")
                        .roleType(RoleScope.TENANT.name())
                        .description("租户默认管理员角色")
                        .build());
            }
            if (roleRepository.findByRoleCode("tenant_user").isEmpty()) {
                roleRepository.save(Role.builder()
                        .roleCode("tenant_user")
                        .roleName("普通用户")
                        .roleType(RoleScope.TENANT.name())
                        .description("租户默认用户角色")
                        .build());
            }
        } finally {
            TenantContext.restore(original);
        }
    }

    private Role getById(Long roleId) {
        return roleRepository.findById(roleId)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.ROLE_NOT_FOUND, "角色不存在: " + roleId));
    }

    private void requireTenant() {
        if (TenantContext.getTenantId() == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "缺少租户上下文");
        }
    }
}
