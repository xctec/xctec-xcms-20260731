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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 审计切面。拦截标注 {@link AuditLog} 的方法，发布 {@link AuditEvent} 由审计模块落库。
 * 操作者身份优先取 SecurityContext 中已认证用户，回退到 TenantContext（异步/非请求线程场景）。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(10)
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;

    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            success = false;
            errorMsg = t.getMessage();
            throw t;
        } finally {
            try {
                publish(pjp, auditLog, success, errorMsg, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.warn("发布审计事件失败: {}", e.getMessage());
            }
        }
    }

    private void publish(ProceedingJoinPoint pjp, AuditLog auditLog, boolean success, String errorMsg, long durationMs) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();

        AuditEvent.AuditEventBuilder builder = AuditEvent.builder()
                .tenantId(TenantContext.getTenantId())
                .eventId(UUID.randomUUID().toString())
                .eventType(auditLog.type())
                .bizModule(auditLog.module())
                .bizId(resolveBizId(pjp))
                .action(method.getName())
                .operatorId(resolveOperatorId())
                .operatorName(resolveOperatorName())
                .ip(resolveIp())
                .success(success)
                .errorMsg(truncate(errorMsg, 500))
                .detail(truncate(auditLog.type(), 2000))
                .durationMs(durationMs)
                .occurTime(LocalDateTime.now());
        // 通过 Spring 应用事件发布，交由审计模块监听落库
        publishEvent(builder.build());
    }

    private void publishEvent(AuditEvent event) {
        eventPublisher.publishEvent(event);
    }

    private Long resolveOperatorId() {
        Long fromTenant = TenantContext.getCurrentUserId();
        if (fromTenant != null) {
            return fromTenant;
        }
        // 回退：尝试从 Spring Security 的 SecurityContext 读取真实登录用户（运行时反射，避免硬依赖 spring-security）
        try {
            Class<?> holder = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Object ctx = holder.getMethod("getContext").invoke(null);
            if (ctx != null) {
                Object auth = ctx.getClass().getMethod("getAuthentication").invoke(ctx);
                if (auth != null) {
                    Object principal = auth.getClass().getMethod("getPrincipal").invoke(auth);
                    if (principal instanceof Long l) {
                        return l;
                    }
                    if (principal instanceof String s && !s.isBlank() && s.chars().allMatch(Character::isDigit)) {
                        return Long.parseLong(s);
                    }
                }
            }
        } catch (Exception ignored) {
            // 忽略：无 Spring Security 场景
        }
        return null;
    }

    private String resolveOperatorName() {
        try {
            Class<?> holder = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Object ctx = holder.getMethod("getContext").invoke(null);
            if (ctx != null) {
                Object auth = ctx.getClass().getMethod("getAuthentication").invoke(ctx);
                if (auth != null) {
                    Object name = auth.getClass().getMethod("getName").invoke(auth);
                    if (name instanceof String s && !s.isBlank()) {
                        return s;
                    }
                }
            }
        } catch (Exception ignored) {
            // 忽略
        }
        return null;
    }

    private String resolveIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                return attrs.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {
            // 非 Web 上下文（如定时任务）
        }
        return null;
    }

    private String resolveBizId(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Long) {
                    return String.valueOf(arg);
                }
            }
        }
        return null;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
