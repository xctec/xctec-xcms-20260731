package com.df4j.xctec.xcms.identity.api.dto.request;

import lombok.Data;

@Data
public class SsoProviderUpdateRequest {
    private Long id;
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
    private Long defaultRole;
    private Boolean enabled;
}
