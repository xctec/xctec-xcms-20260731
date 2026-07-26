package com.df4j.xctec.xcms.identity.api.dto.request;

import lombok.Data;

@Data
public class SsoAuthorizeRequest {
    private String serverCode;
    private String state;
}
