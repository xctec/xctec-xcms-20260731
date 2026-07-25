package com.df4j.xctec.xcms.portal.service;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import com.df4j.xctec.xcms.auth.api.enums.MenuScope;
import com.df4j.xctec.xcms.identity.api.UserService;
import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import com.df4j.xctec.xcms.identity.api.dto.UserDTO;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.portal.api.PortalService;
import com.df4j.xctec.xcms.portal.api.dto.PortalNavigationDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalProfileDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalQuickEntryDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalSurfaceMenusDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalWorkbenchDTO;
import com.df4j.xctec.xcms.portal.domain.PortalQuickEntry;
import com.df4j.xctec.xcms.portal.repository.PortalQuickEntryRepository;
import com.df4j.xctec.xcms.tenant.api.TenantService;
import com.df4j.xctec.xcms.tenant.api.dto.TenantDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 门户服务实现。聚合授权（菜单/权限）、身份（用户/角色）、租户（租户名）能力，
 * 提供统一入口导航与可自定义的门户工作台。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalServiceImpl implements PortalService {

    private static final List<String> ALL_SURFACES = List.of("ADMIN", "BUSINESS");

    private final PermissionService permissionService;
    private final UserService userService;
    private final TenantService tenantService;
    private final PortalQuickEntryRepository quickEntryRepository;

    @Override
    public PortalNavigationDTO getNavigation(Long userId) {
        PortalProfileDTO profile = buildProfile(userId);
        List<PortalSurfaceMenusDTO> surfaces = new ArrayList<>();
        List<String> accessible = new ArrayList<>();
        for (String scope : ALL_SURFACES) {
            List<MenuDTO> menus = permissionService.getUserMenus(userId, MenuScope.valueOf(scope));
            if (menus != null && !menus.isEmpty()) {
                PortalSurfaceMenusDTO surfaceMenus = new PortalSurfaceMenusDTO();
                surfaceMenus.setSurface(scope);
                surfaceMenus.setMenus(menus);
                surfaces.add(surfaceMenus);
                accessible.add(scope);
            }
        }
        profile.setSurfaces(accessible);
        PortalNavigationDTO navigation = new PortalNavigationDTO();
        navigation.setProfile(profile);
        navigation.setSurfaces(surfaces);
        return navigation;
    }

    @Override
    public List<MenuDTO> getMenus(Long userId, String scope) {
        return permissionService.getUserMenus(userId, toScope(scope));
    }

    @Override
    public PortalProfileDTO getProfile(Long userId) {
        PortalProfileDTO profile = buildProfile(userId);
        profile.setSurfaces(computeAccessibleSurfaces(userId));
        return profile;
    }

    @Override
    public List<PortalQuickEntryDTO> getQuickEntries(Long userId, String surface) {
        List<PortalQuickEntry> list = quickEntryRepository
                .findByUserIdAndSurfaceAndDeletedAtIsNull(userId, surface, Sort.by(Sort.Direction.ASC, "sortOrder"));
        return list.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public List<PortalQuickEntryDTO> saveQuickEntries(Long userId, String surface, List<PortalQuickEntryDTO> entries) {
        if (!ALL_SURFACES.contains(surface)) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "非法的门户面：" + surface);
        }
        List<PortalQuickEntry> existing = quickEntryRepository
                .findByUserIdAndSurfaceAndDeletedAtIsNull(userId, surface, Sort.unsorted());
        quickEntryRepository.deleteAll(existing);

        List<PortalQuickEntry> toSave = (entries == null ? List.<PortalQuickEntryDTO>of() : entries).stream()
                .map(e -> toEntity(e, userId, surface))
                .toList();
        return quickEntryRepository.saveAll(toSave).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public void deleteQuickEntry(Long userId, Long id) {
        PortalQuickEntry entry = quickEntryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "快捷入口不存在"));
        if (!entry.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "无权操作该快捷入口");
        }
        entry.setDeletedAt(LocalDateTime.now());
        quickEntryRepository.save(entry);
    }

    @Override
    public PortalWorkbenchDTO getWorkbench(Long userId, String surface) {
        PortalProfileDTO profile = getProfile(userId);
        List<PortalSurfaceMenusDTO> surfaces = profile.getSurfaces().stream()
                .map(s -> {
                    PortalSurfaceMenusDTO m = new PortalSurfaceMenusDTO();
                    m.setSurface(s);
                    m.setMenus(permissionService.getUserMenus(userId, MenuScope.valueOf(s)));
                    return m;
                })
                .toList();
        String wbSurface = StringUtils.hasText(surface)
                ? surface
                : (profile.getSurfaces().isEmpty() ? "ADMIN" : profile.getSurfaces().get(0));
        List<PortalQuickEntryDTO> quickEntries = getQuickEntries(userId, wbSurface);

        PortalWorkbenchDTO workbench = new PortalWorkbenchDTO();
        workbench.setProfile(profile);
        workbench.setSurfaces(surfaces);
        workbench.setQuickEntries(quickEntries);
        return workbench;
    }

    private PortalProfileDTO buildProfile(Long userId) {
        UserDTO user = userService.getUserById(userId);
        PortalProfileDTO profile = new PortalProfileDTO();
        profile.setUserId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setRealName(user.getRealName());
        profile.setAvatar(user.getAvatar());
        profile.setTenantId(user.getTenantId());
        if (user.getTenantId() != null) {
            try {
                TenantDTO tenant = tenantService.getTenantById(user.getTenantId());
                profile.setTenantName(tenant != null ? tenant.getTenantName() : null);
            } catch (Exception e) {
                log.warn("获取租户名称失败 tenantId={}", user.getTenantId(), e);
            }
        }
        List<String> roleNames = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream().map(RoleDTO::getRoleName).toList();
        profile.setRoleNames(roleNames);
        return profile;
    }

    private List<String> computeAccessibleSurfaces(Long userId) {
        List<String> accessible = new ArrayList<>();
        for (String scope : ALL_SURFACES) {
            List<MenuDTO> menus = permissionService.getUserMenus(userId, MenuScope.valueOf(scope));
            if (menus != null && !menus.isEmpty()) {
                accessible.add(scope);
            }
        }
        return accessible;
    }

    private MenuScope toScope(String scope) {
        if (!StringUtils.hasText(scope)) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "门户面 scope 不能为空");
        }
        try {
            return MenuScope.valueOf(scope.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "非法的门户面：" + scope);
        }
    }

    private PortalQuickEntryDTO toDTO(PortalQuickEntry e) {
        PortalQuickEntryDTO dto = new PortalQuickEntryDTO();
        dto.setId(e.getId());
        dto.setSurface(e.getSurface());
        dto.setTitle(e.getTitle());
        dto.setIcon(e.getIcon());
        dto.setUrl(e.getUrl());
        dto.setTarget(e.getTarget());
        dto.setSortOrder(e.getSortOrder());
        dto.setEnabled(e.getEnabled());
        return dto;
    }

    private PortalQuickEntry toEntity(PortalQuickEntryDTO dto, Long userId, String surface) {
        return PortalQuickEntry.builder()
                .userId(userId)
                .surface(surface)
                .title(dto.getTitle())
                .icon(dto.getIcon())
                .url(dto.getUrl())
                .target(dto.getTarget() == null ? "SELF" : dto.getTarget())
                .sortOrder(dto.getSortOrder())
                .enabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled())
                .build();
    }
}
