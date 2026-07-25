package com.df4j.xctec.xcms.kernel.common;

import lombok.Data;

/**
 * 分页查询基类。
 */
@Data
public class PageQuery {

    private int page = 1;
    private int size = 20;
    private String sortBy;
    private String sortOrder = "ASC";

    public int getOffset() {
        return (page - 1) * size;
    }
}
