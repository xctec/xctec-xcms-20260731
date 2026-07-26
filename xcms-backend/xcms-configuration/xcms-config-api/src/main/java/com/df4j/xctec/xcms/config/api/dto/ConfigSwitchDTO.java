package com.df4j.xctec.xcms.config.api.dto;

import lombok.Data;

@Data
public class ConfigSwitchDTO {
    private String featureCode;
    private String description;
    private boolean enabled;
    private String config;
}
