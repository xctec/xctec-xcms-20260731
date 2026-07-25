package com.df4j.xctec.xcms.file.api.dto;

import lombok.Data;

@Data
public class FileQuery {
    private String bizModule;
    private String bizId;
    private int page = 1;
    private int size = 20;
}
