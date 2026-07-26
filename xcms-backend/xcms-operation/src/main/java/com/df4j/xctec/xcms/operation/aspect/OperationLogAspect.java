package com.df4j.xctec.xcms.operation.aspect;

import com.df4j.xctec.xcms.operation.api.OperationLogService;
import com.df4j.xctec.xcms.operation.api.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 拦截 @OperationLog 注解的方法，自动记录操作日志。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService logService;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OperationLog opLog) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = true;
        String err = null;
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            success = false;
            err = t.getMessage();
            throw t;
        } finally {
            long dur = System.currentTimeMillis() - start;
            String module = opLog.module().isEmpty() ? defaultModule(pjp) : opLog.module();
            String action = opLog.action().isEmpty()
                    ? ((MethodSignature) pjp.getSignature()).getName() : opLog.action();
            logService.record(module, action, opLog.bizType(), null, null, success, err, dur);
        }
    }

    private String defaultModule(ProceedingJoinPoint pjp) {
        String cls = pjp.getTarget().getClass().getSimpleName();
        return cls.replace("ServiceImpl", "").replace("Service", "");
    }
}
