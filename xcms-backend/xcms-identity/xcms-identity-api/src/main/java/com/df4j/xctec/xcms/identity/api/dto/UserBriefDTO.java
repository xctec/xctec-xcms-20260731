package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 用户简要信息（跨模块复用）
 */
@Data
public class UserBriefDTO {
    private Long id;
    private String username;
    private String realName;
    private Long deptId;
    private String deptName;
}
