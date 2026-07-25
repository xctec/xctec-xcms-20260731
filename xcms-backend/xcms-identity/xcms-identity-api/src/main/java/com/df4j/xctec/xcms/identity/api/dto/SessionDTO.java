package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话信息
 */
@Data
public class SessionDTO {
    private Long id;
    private Long userId;
    private Long tenantId;
    private String deviceType;
    private String deviceInfo;
    private String loginIp;
    private LocalDateTime loginAt;
    private LocalDateTime expireAt;
    private String status;
}
