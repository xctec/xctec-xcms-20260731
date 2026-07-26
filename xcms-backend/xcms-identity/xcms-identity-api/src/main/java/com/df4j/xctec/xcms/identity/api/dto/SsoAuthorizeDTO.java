package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

@Data
public class SsoAuthorizeDTO {
    private String serverCode;
    private String redirectUrl;
}
