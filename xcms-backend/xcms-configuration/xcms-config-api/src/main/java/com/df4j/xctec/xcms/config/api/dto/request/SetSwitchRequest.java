package com.df4j.xctec.xcms.config.api.dto.request;

import lombok.Data;

@Data
public class SetSwitchRequest {
    private String featureCode;
    private boolean enabled;
}
