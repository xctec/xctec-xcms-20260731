package com.df4j.xctec.xcms.config.api.dto.request;

import lombok.Data;

@Data
public class ConfigSwitchCreateRequest {
    private String switchKey;
    private String switchName;
    private boolean enabled;
    private String switchValue;
    private String remark;
}
