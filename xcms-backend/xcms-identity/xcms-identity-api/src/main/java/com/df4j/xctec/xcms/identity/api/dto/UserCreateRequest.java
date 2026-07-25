package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建用户请求
 */
@Data
public class UserCreateRequest {
    private String username;
    private String password;
    private String realName;
    private String employeeNo;
    private String email;
    private String phone;
    private List<Long> roleIds;
    private Long deptId;
    private Long positionId;
}
