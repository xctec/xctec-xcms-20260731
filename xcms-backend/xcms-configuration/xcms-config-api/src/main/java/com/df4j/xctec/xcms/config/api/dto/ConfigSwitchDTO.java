package com.df4j.xctec.xcms.config.api.dto;

import lombok.Data;

@Data
public class ConfigSwitchDTO {
    private String switchKey;
    private String switchName;
    private boolean enabled;
    private String switchValue;
    private String remark;
}
