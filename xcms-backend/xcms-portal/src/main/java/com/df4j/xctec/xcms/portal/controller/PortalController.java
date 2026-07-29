package com.df4j.xctec.xcms.portal.controller;

import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.portal.api.PortalService;
import com.df4j.xctec.xcms.portal.api.dto.PortalNavigationDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalProfileDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalQuickEntryDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalWorkbenchDTO;
import com.df4j.xctec.xcms.portal.api.dto.request.PortalMenuQuery;
import com.df4j.xctec.xcms.portal.api.dto.request.PortalQuickEntryQuery;
import com.df4j.xctec.xcms.portal.api.dto.request.PortalQuickEntrySaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门户统一入口与导航控制器。当前用户由 {@link TenantContext} 提供。
 */
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;

    @PostMapping("/navigation")
    public ApiResponse<PortalNavigationDTO> navigation() {
        return ApiResponse.success(portalService.getNavigation(currentUserId()));
    }

    @PostMapping("/menus")
    public ApiResponse<List<MenuDTO>> menus(@RequestBody PortalMenuQuery query) {
        return ApiResponse.success(portalService.getMenus(currentUserId(), query.getScope()));
    }

    @PostMapping("/profile")
    public ApiResponse<PortalProfileDTO> profile() {
        return ApiResponse.success(portalService.getProfile(currentUserId()));
    }

    @PostMapping("/quick-entry/list")
    public ApiResponse<List<PortalQuickEntryDTO>> listQuickEntries(@RequestBody PortalQuickEntryQuery query) {
        return ApiResponse.success(portalService.getQuickEntries(currentUserId(), query.getSurface()));
    }

    @PostMapping("/quick-entry/save")
    public ApiResponse<List<PortalQuickEntryDTO>> saveQuickEntries(@RequestBody PortalQuickEntrySaveRequest request) {
        return ApiResponse.success(portalService.saveQuickEntries(currentUserId(), request.getSurface(), request.getEntries()));
    }

    @PostMapping("/quick-entry/delete")
    public ApiResponse<Void> deleteQuickEntry(@RequestBody IdRequest request) {
        portalService.deleteQuickEntry(currentUserId(), request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/workbench")
    public ApiResponse<PortalWorkbenchDTO> workbench(@RequestBody PortalQuickEntryQuery query) {
        return ApiResponse.success(portalService.getWorkbench(currentUserId(), query.getSurface()));
    }

    private Long currentUserId() {
        Long userId = TenantContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID, "未登录或会话已失效");
        }
        return userId;
    }
}
