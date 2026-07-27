package com.df4j.xctec.xcms.kernel.event;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 领域事件统一分发器。
 *
 * <p>收集所有 {@link DomainEventListener} Bean，按 {@link DomainEventListener#eventType()}
 * 建立路由表；{@link #dispatch} 时按事件运行时类型路由到对应监听器，并集中处理横切关注点：</p>
 * <ol>
 *   <li>租户切换：事件携带 {@link DomainEvent#tenantId()} 时，进入监听器前
 *       {@code TenantContext.switchTo}，退出后恢复，消除各监听器手动切换的不一致；</li>
 *   <li>幂等去重：事件携带 {@link DomainEvent#eventId()} 时，经 {@link EventDedupPort}
 *       判定，重复事件跳过，防止 at-least-once 投递下的重复副作用；</li>
 *   <li>异常捕获 + 日志：单个监听器失败不阻断其余监听器，记录后聚合抛出（不吞，交重试机制）。</li>
 * </ol>
 *
 * <p>本类为纯 POJO，不依赖 Spring 事件总线，可直接单元测试。进程内由
 * {@link SpringDomainEventBridge} 桥接触发；微服务形态由 MQ 消费适配器触发。</p>
 */
public class DomainEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventDispatcher.class);

    private final Map<Class<? extends DomainEvent>, List<DomainEventListener<? extends DomainEvent>>> routes = new HashMap<>();
    private final EventDedupPort dedupPort;

    public DomainEventDispatcher(List<DomainEventListener<? extends DomainEvent>> listeners) {
        this(listeners, null);
    }

    public DomainEventDispatcher(List<DomainEventListener<? extends DomainEvent>> listeners, EventDedupPort dedupPort) {
        for (DomainEventListener<? extends DomainEvent> listener : listeners) {
            routes.computeIfAbsent(listener.eventType(), k -> new ArrayList<>()).add(listener);
        }
        this.dedupPort = dedupPort;
    }

    /**
     * 分发事件到所有订阅该类型的监听器。
     *
     * @param event 领域事件
     */
    @SuppressWarnings("unchecked")
    public void dispatch(DomainEvent event) {
        if (event == null) {
            return;
        }
        List<DomainEventListener<? extends DomainEvent>> listeners = routes.get(event.getClass());
        if (listeners == null || listeners.isEmpty()) {
            log.debug("事件 {} 无订阅监听器，跳过", event.topic());
            return;
        }
        Long tenantId = event.tenantId();
        TenantContext.TenantInfo original = null;
        boolean switched = false;
        if (tenantId != null) {
            original = TenantContext.switchTo(tenantId);
            switched = true;
        }
        try {
            RuntimeException first = null;
            String eventId = event.eventId();
            for (DomainEventListener<? extends DomainEvent> listener : listeners) {
                if (dedupPort != null && eventId != null
                        && !dedupPort.tryMarkProcessed(listener.getClass().getName(), eventId)) {
                    log.info("事件 {}({}) 已由 {} 处理过，幂等跳过",
                            event.topic(), eventId, listener.getClass().getSimpleName());
                    continue;
                }
                try {
                    ((DomainEventListener<DomainEvent>) listener).onEvent(event);
                } catch (RuntimeException ex) {
                    log.error("监听器 {} 处理事件 {} 失败: {}",
                            listener.getClass().getSimpleName(), event.topic(), ex.getMessage(), ex);
                    if (first == null) {
                        first = ex;
                    }
                }
            }
            if (first != null) {
                // 不吞异常：向上抛出交由重试/告警机制处理
                throw first;
            }
        } finally {
            if (switched) {
                TenantContext.restore(original);
            }
        }
    }
}
