package com.df4j.xctec.xcms.portal.api;

import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalNavigationDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalProfileDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalQuickEntryDTO;
import com.df4j.xctec.xcms.portal.api.dto.PortalWorkbenchDTO;

import java.util.List;

/**
 * 门户服务：统一入口与导航、用户档案、门户工作台（含可自定义快捷入口）。
 */
public interface PortalService {

    /**
     * 获取门户导航模型：用户档案 + 各可访问门户面菜单。
     */
    PortalNavigationDTO getNavigation(Long userId);

    /**
     * 获取指定门户面下、当前用户可见的菜单树。
     */
    List<MenuDTO> getMenus(Long userId, String scope);

    /**
     * 获取当前用户的门户档案（含可访问门户面）。
     */
    PortalProfileDTO getProfile(Long userId);

    /**
     * 获取用户在某门户面下的自定义快捷入口。
     */
    List<PortalQuickEntryDTO> getQuickEntries(Long userId, String surface);

    /**
     * 覆盖保存用户在指定门户面下的快捷入口。
     */
    List<PortalQuickEntryDTO> saveQuickEntries(Long userId, String surface, List<PortalQuickEntryDTO> entries);

    /**
     * 删除用户的一个快捷入口。
     */
    void deleteQuickEntry(Long userId, Long id);

    /**
     * 获取门户工作台聚合信息。
     */
    PortalWorkbenchDTO getWorkbench(Long userId, String surface);
}
