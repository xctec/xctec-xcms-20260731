package com.df4j.xctec.xcms.audit.listener;

import com.df4j.xctec.xcms.audit.api.event.AuditEvent;
import com.df4j.xctec.xcms.audit.domain.AuditLog;
import com.df4j.xctec.xcms.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 审计事件监听器。捕获各模块发布的 {@link AuditEvent} 并落库。
 * 与各模块解耦：模块只负责发布事件，持久化由本监听器完成。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @EventListener(AuditEvent.class)
    @Order(0)
    public void onAuditEvent(AuditEvent event) {
        try {
            AuditLog log = new AuditLog();
            log.setEventId(event.getEventId());
            log.setBizModule(event.getBizModule());
            log.setEventType(event.getEventType());
            log.setBizId(event.getBizId());
            log.setAction(event.getAction());
            log.setOperatorId(event.getOperatorId());
            log.setOperatorName(event.getOperatorName());
            log.setIp(event.getIp());
            log.setSuccess(event.isSuccess());
            log.setErrorMsg(event.getErrorMsg());
            log.setDetail(event.getDetail());
            log.setDurationMs(event.getDurationMs());
            log.setOccurTime(event.getOccurTime() == null ? java.time.LocalDateTime.now() : event.getOccurTime());
            auditLogRepository.save(log);
        } catch (Exception e) {
            // 审计落库失败不应影响主流程
            log.error("审计日志落库失败: {}", e.getMessage(), e);
        }
    }
}
