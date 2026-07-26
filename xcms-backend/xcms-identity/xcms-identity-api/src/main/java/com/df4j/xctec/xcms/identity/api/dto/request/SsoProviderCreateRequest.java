package com.df4j.xctec.xcms.identity.api.dto.request;

import lombok.Data;

@Data
public class SsoProviderCreateRequest {
    private Long tenantId;
    private String serverCode;
    private String serverName;
    private String protocol;
    private String clientId;
    private String clientSecret;
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
    private Boolean autoCreate;
    private String defaultRole;
    private Boolean enabled;
}
