package com.df4j.xctec.xcms.portal.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 门户用户档案：当前用户的基础信息、所属租户、角色与可访问的门户面。
 */
@Data
public class PortalProfileDTO {

    private Long userId;
    private String username;
    private String realName;
    private String avatar;

    private Long tenantId;
    private String tenantName;

    /** 角色名称列表 */
    private List<String> roleNames;

    /** 当前用户可访问的门户面：ADMIN / BUSINESS */
    private List<String> surfaces;
}
