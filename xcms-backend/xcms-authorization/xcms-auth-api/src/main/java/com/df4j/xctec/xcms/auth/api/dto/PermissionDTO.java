package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

@Data
public class PermissionDTO {
    private Long id;
    private String permCode;
    private String permName;
    private String permType;
    private String module;
    private String action;
}
