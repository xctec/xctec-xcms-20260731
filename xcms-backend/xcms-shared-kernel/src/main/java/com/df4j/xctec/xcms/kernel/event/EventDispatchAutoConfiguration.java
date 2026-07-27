package com.df4j.xctec.xcms.kernel.event;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

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
                                                       EventDedupPort eventDedupPort,
                                                       ObjectProvider<PlatformTransactionManager> txManager) {
        // AFTER_COMMIT 阶段原事务已提交，监听器写库需 REQUIRES_NEW 新事务；
        // 新事务在分发器完成租户切换后开启，@TenantId 捕获事件租户（ADR-015）
        TransactionOperations requiresNewTx = null;
        PlatformTransactionManager tm = txManager.getIfAvailable();
        if (tm != null) {
            TransactionTemplate template = new TransactionTemplate(tm);
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            requiresNewTx = template;
        }
        return new DomainEventDispatcher(listeners.orderedStream()
                .map(l -> (DomainEventListener<? extends DomainEvent>) l)
                .collect(Collectors.toList()), eventDedupPort, requiresNewTx);
    }

    @Bean
    @ConditionalOnMissingBean(SpringDomainEventBridge.class)
    public SpringDomainEventBridge springDomainEventBridge(DomainEventDispatcher dispatcher) {
        return new SpringDomainEventBridge(dispatcher);
    }
}
