package com.df4j.xctec.xcms.app;

import com.df4j.xctec.xcms.kernel.context.ActorContext;
import com.df4j.xctec.xcms.kernel.context.ActorContextTaskDecorator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ActorContext 跨线程传播测试（AT-04，ADR-012/014）。
 */
class ActorContextTaskDecoratorTest {

    private final ActorContextTaskDecorator decorator = new ActorContextTaskDecorator();

    @AfterEach
    void tearDown() {
        ActorContext.clear();
    }

    @Test
    void asyncTaskInheritsActorContext() throws Exception {
        ActorContext.setUser(101L, 7L);
        AtomicReference<Long> tenantInWorker = new AtomicReference<>();
        AtomicReference<Long> userInWorker = new AtomicReference<>();

        Runnable decorated = decorator.decorate(() -> {
            tenantInWorker.set(ActorContext.getTenantId());
            userInWorker.set(ActorContext.getCurrentUserId());
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(decorated).get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
        }

        assertEquals(101L, tenantInWorker.get());
        assertEquals(7L, userInWorker.get());
    }

    @Test
    void workerThreadContextClearedAfterExecution() throws Exception {
        ActorContext.setUser(101L, 7L);
        Runnable decorated = decorator.decorate(() -> {
        });
        AtomicReference<Long> tenantAfter = new AtomicReference<>(-1L);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(decorated).get(5, TimeUnit.SECONDS);
            // 同一工作线程再跑一个未装饰任务：上下文应已被清理
            executor.submit(() -> tenantAfter.set(ActorContext.getTenantId())).get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
        }

        assertNull(tenantAfter.get());
        // 提交线程自身上下文不受影响
        assertTrue(ActorContext.getTenantId() == 101L);
    }
}
