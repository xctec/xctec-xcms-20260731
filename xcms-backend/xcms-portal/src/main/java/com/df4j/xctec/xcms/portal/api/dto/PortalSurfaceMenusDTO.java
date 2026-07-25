package com.df4j.xctec.xcms.portal.api.dto;

import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import lombok.Data;

import java.util.List;

/**
 * 单个门户面下的菜单树。
 */
@Data
public class PortalSurfaceMenusDTO {

    /** 门户面：ADMIN / BUSINESS */
    private String surface;

    private List<MenuDTO> menus;
}
