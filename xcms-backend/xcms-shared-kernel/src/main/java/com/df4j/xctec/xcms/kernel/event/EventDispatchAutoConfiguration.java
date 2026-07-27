package com.df4j.xctec.xcms.kernel.event;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

/**
 * 事件消费端（分发器 + 进程内桥接）自动配置，与 {@link EventPublisherAutoConfiguration} 对称。
 */
@Configuration
public class EventDispatchAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EventDedupPort.class)
    public EventDedupPort eventDedupPort() {
        return new InMemoryEventDedup();
    }

    @Bean
    @ConditionalOnMissingBean(DomainEventDispatcher.class)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public DomainEventDispatcher domainEventDispatcher(ObjectProvider<DomainEventListener> listeners,
                                                       EventDedupPort eventDedupPort) {
        return new DomainEventDispatcher(listeners.orderedStream()
                .map(l -> (DomainEventListener<? extends DomainEvent>) l)
                .collect(Collectors.toList()), eventDedupPort);
    }

    @Bean
    @ConditionalOnMissingBean(SpringDomainEventBridge.class)
    public SpringDomainEventBridge springDomainEventBridge(DomainEventDispatcher dispatcher) {
        return new SpringDomainEventBridge(dispatcher);
    }
}
