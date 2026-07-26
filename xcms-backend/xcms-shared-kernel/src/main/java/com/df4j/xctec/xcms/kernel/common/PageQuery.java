package com.df4j.xctec.xcms.kernel.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询基类。
 */
@Data
public class PageQuery {

    @Schema(description = "页码，从 1 开始", example = "1")
    private int page = 1;
    @Schema(description = "每页条数", example = "20")
    private int size = 20;
    @Schema(description = "排序字段（对应实体属性名，如 id、createTime）", example = "createTime")
    private String sortBy;
    @Schema(description = "排序方向（ASC/DESC）", example = "ASC")
    private String sortOrder = "ASC";

    public long getOffset() {
        return (long) (page - 1) * size;
    }
}
