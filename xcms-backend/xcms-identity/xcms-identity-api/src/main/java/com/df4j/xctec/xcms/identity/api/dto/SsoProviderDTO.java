package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SsoProviderDTO {
    private Long id;
    private Long tenantId;
    private String serverCode;
    private String serverName;
    private String protocol;
    private String clientId;
    private String authorizeUrl;
    private String tokenUrl;
    private String userInfoUrl;
    private String redirectUri;
    private String logoutUrl;
    private String idpUserIdField;
    private String usernameField;
    private String emailField;
    private String nameField;
    private String scope;
    private boolean autoCreate;
    private String defaultRole;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
