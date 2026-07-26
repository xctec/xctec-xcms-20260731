package com.df4j.xctec.xcms.audit.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级审计注解。标注后由 {@code AuditAspect} 拦截，
 * 自动发布 {@link com.df4j.xctec.xcms.audit.api.event.AuditEvent} 交由审计模块记录。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /** 业务模块，如 user / order */
    String module() default "";

    /** 业务类型，如 LOGIN / CREATE */
    String type() default "";

    /** 操作动作描述 */
    String action() default "";

    /** 是否记录入参 */
    boolean recordArgs() default false;

    /** 是否记录出参 */
    boolean recordResult() default false;

    /** 发生异常时是否仍记录（记录为失败），默认 true 表示记录后继续抛出 */
    boolean ignoreExceptions() default true;
}
