package com.df4j.xctec.xcms.portal.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 门户工作台聚合：用户档案、可访问门户面菜单、用户自定义快捷入口。
 */
@Data
public class PortalWorkbenchDTO {

    private PortalProfileDTO profile;

    private List<PortalSurfaceMenusDTO> surfaces;

    private List<PortalQuickEntryDTO> quickEntries;
}
