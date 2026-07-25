package com.df4j.xctec.xcms.audit.aspect;

import com.df4j.xctec.xcms.audit.api.annotation.AuditLog;
import com.df4j.xctec.xcms.audit.api.event.AuditEvent;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 审计切面。拦截标注 {@link AuditLog} 的方法，自动发布 {@link AuditEvent}，
 * 由审计监听器负责落库。模块无需直接依赖审计实现，仅声明注解即可。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        String ip = resolveClientIp();
        Long operatorId = TenantContext.getCurrentUserId();
        boolean success = true;
        String errorMsg = null;
        Throwable thrown = null;
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            success = false;
            errorMsg = t.getMessage();
            thrown = t;
        }

        String detail = null;
        if (auditLog.recordArgs()) {
            try {
                detail = java.util.Arrays.toString(joinPoint.getArgs());
            } catch (Exception ignored) {
            }
        }

        AuditEvent event = AuditEvent.builder()
                .tenantId(TenantContext.getTenantId())
                .operatorId(operatorId)
                .bizModule(auditLog.module())
                .bizType(auditLog.type())
                .bizId(resolveBizId(joinPoint))
                .action(auditLog.action())
                .ip(ip)
                .success(success)
                .errorMsg(errorMsg)
                .detail(detail)
                .durationMs(System.currentTimeMillis() - start)
                .occurTime(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(event);

        if (thrown != null) {
            if (auditLog.ignoreExceptions()) {
                throw thrown;
            }
            // 不忽略异常时仅记录，不重复抛出（避免吞掉原始异常栈）
            throw thrown;
        }
        return result;
    }

    private String resolveClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                return attrs.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String resolveBizId(ProceedingJoinPoint joinPoint) {
        // 约定：若方法第一个参数类型为 Long，则作为 bizId
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0 && args[0] instanceof Long) {
            return String.valueOf(args[0]);
        }
        return null;
    }

    @SuppressWarnings("unused")
    private String currentMethod(ProceedingJoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getName();
    }
}
