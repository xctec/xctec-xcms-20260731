package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class MenuDTO {
    private Long id;
    private String menuCode;
    private String menuName;
    private String menuType;
    private String path;
    private String icon;
    private Integer sortOrder;
    private List<MenuDTO> children;
}
