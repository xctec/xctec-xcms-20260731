package com.df4j.xctec.xcms.identity.api.dto;

import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情
 */
@Data
public class UserDTO {
    private Long id;
    private Long tenantId;
    private String username;
    private String realName;
    private String employeeNo;
    private String email;
    private String phone;
    private String avatar;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private List<RoleDTO> roles;
}
