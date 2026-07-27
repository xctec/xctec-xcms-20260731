package com.df4j.xctec.xcms.kernel.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记该接口返回的数据需要进行列级脱敏。
 *
 * @deprecated AT-18 起脱敏下沉 DTO 层：请在 DTO 字段上使用
 * {@code com.df4j.xctec.xcms.datapermission.api.MaskField} 声明敏感列，
 * 由 MaskFieldResponseAdvice 统一执行（fail-closed）。本注解已无消费者，仅为兼容保留。
 */
@Deprecated
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskResource {
    String value();
}
