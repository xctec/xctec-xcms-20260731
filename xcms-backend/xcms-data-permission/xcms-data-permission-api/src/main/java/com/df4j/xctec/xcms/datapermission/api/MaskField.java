package com.df4j.xctec.xcms.datapermission.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 列级脱敏字段标注（ADR-013 / AT-16，AT-18 在 DTO 映射层消费）。
 *
 * <p>标注在 DTO 字段上，声明该字段属于哪个资源类型的敏感列；
 * 脱敏在 toDTO 之后执行（fail-closed：缺规则默认脱敏，不裸奔）。</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MaskField {

    /** 资源类型（与脱敏规则的 resourceType 对应） */
    String resourceType();

    /** 规则中的字段名，缺省取被标注字段名 */
    String field() default "";
}
