package com.df4j.xctec.xcms.kernel.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记该接口返回的数据需要进行列级脱敏。
 * value 指定资源类型（resourceType），用于匹配 ColumnMask 脱敏规则。
 * 由 {@code ColumnMaskResponseBodyAdvice} 自动在响应写出前执行脱敏。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskResource {
    String value();
}
