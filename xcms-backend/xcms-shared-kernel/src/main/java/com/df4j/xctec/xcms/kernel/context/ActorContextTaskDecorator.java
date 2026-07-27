package com.df4j.xctec.xcms.kernel.context;

import org.springframework.core.task.TaskDecorator;

/**
 * 异步任务的 {@link ActorContext} 传播装饰器（AT-04，ADR-012/014）。
 *
 * <p>ThreadLocal 上下文不会跨线程传递，@Async 方法内 tenantId/principal 会丢失，
 * 导致 @TenantId 过滤失效与审计缺失。本装饰器在任务<b>提交时</b>捕获主线程的
 * {@link ActorContext.Actor} 快照，在工作线程执行前恢复、执行后清理，
 * 防止线程池复用导致的上下文串染。</p>
 */
public class ActorContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 提交任务的线程：捕获当前执行者快照
        ActorContext.Actor snapshot = ActorContext.current();
        return () -> {
            // 工作线程：恢复快照 → 执行 → 清理（防线程池复用串染）
            ActorContext.restore(snapshot);
            try {
                runnable.run();
            } finally {
                ActorContext.clear();
            }
        };
    }
}
