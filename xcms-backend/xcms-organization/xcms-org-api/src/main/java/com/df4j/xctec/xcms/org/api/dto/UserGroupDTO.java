package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

/**
 * 用户组信息
 */
@Data
public class UserGroupDTO {
    private Long id;
    private String groupName;
    private String description;
    private String type;
    private Integer memberCount;
}
