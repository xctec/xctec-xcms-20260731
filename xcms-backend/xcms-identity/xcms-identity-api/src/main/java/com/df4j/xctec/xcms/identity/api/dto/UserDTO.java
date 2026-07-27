package com.df4j.xctec.xcms.identity.api.dto;

import com.df4j.xctec.xcms.datapermission.api.MaskField;
import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情
 */
@Data
public class UserDTO {
    @Schema(description = "用户 ID")
    private Long id;

    @Schema(description = "所属租户 ID")
    private Long tenantId;

    @Schema(description = "所属租户名称（登录/用户查询时回填，需后端引入租户服务）")
    private String tenantName;

    @Schema(description = "登录用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "邮箱")
    @MaskField(resourceType = "user")
    private String email;

    @Schema(description = "手机号")
    @MaskField(resourceType = "user")
    private String phone;

    @Schema(description = "头像 URL")
    private String avatar;

    private UserStatus status;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "最近登录 IP")
    private String lastLoginIp;

    @Schema(description = "用户关联的角色列表")
    private List<RoleDTO> roles;
}
