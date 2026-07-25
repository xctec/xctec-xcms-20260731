package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量导入结果
 */
@Data
public class BatchImportResult {
    private int total;
    private int success;
    private int failed;
    private List<String> errors;
}
