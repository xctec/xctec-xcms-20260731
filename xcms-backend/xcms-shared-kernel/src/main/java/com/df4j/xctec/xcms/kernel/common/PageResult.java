package com.df4j.xctec.xcms.kernel.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果。
 * 只返回 list 和 total，page/size 由请求方自行维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 数据列表 */
    private List<T> list;

    /** 总记录数 */
    private long total;

    public static <T> PageResult<T> of(List<T> list, long total) {
        return new PageResult<>(list, total);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(List.of(), 0);
    }
}
