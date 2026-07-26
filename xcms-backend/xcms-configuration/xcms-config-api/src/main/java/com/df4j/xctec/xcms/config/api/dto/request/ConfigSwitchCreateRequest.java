package com.df4j.xctec.xcms.config.api.dto.request;

import lombok.Data;

@Data
public class ConfigSwitchCreateRequest {
    private String featureCode;
    private String description;
    private boolean enabled;
    private String config;
}
