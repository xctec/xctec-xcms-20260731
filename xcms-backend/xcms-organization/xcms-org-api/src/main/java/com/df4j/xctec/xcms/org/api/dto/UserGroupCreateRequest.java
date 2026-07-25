package com.df4j.xctec.xcms.org.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建用户组请求
 */
@Data
public class UserGroupCreateRequest {
    private String groupName;
    private String description;
    private String type;
    private List<Long> memberIds;
}
