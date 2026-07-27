package com.df4j.xctec.xcms.audit.api.event;

import com.df4j.xctec.xcms.kernel.event.BaseDomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计事件。各业务模块通过 {@code ApplicationEventPublisher.publishEvent(...)} 发布该事件，
 * 审计模块监听并落库，从而实现「模块仅发布事件、审计模块负责持久化」的解耦配合。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditEvent extends BaseDomainEvent {

    private Long tenantId;
    private String eventId;
    private Long operatorId;
    private String operatorName;
    private String bizModule;
    private String eventType;
    private String bizId;
    private String action;
    private String ip;
    private boolean success;
    private String errorMsg;
    private String detail;
    private long durationMs;
    private LocalDateTime occurTime;

    @Override
    public String topic() {
        return "audit." + (bizModule == null ? "unknown" : bizModule);
    }

    /** 业务方显式指定则优先；未指定时回退基类自动生成的 UUID，保证去重 ID 始终非空 */
    @Override
    public String eventId() {
        return eventId != null ? eventId : super.eventId();
    }

    @Override
    public Long tenantId() {
        return tenantId;
    }
}
