package com.df4j.xctec.xcms.portal.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 门户导航模型：用户档案 + 各可访问门户面下的菜单树。门户统一入口的核心返回结构。
 */
@Data
public class PortalNavigationDTO {

    private PortalProfileDTO profile;

    /** 仅包含当前用户有权限访问的门户面及其菜单 */
    private List<PortalSurfaceMenusDTO> surfaces;
}
