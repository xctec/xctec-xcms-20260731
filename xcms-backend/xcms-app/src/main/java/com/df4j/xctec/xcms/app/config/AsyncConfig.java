package com.df4j.xctec.xcms.app.config;

import com.df4j.xctec.xcms.kernel.context.ActorContextTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步执行配置（AT-04，ADR-012/014）。
 *
 * <p>启用 @Async 并配置默认执行器：TaskDecorator 在任务提交时捕获
 * ActorContext 快照、工作线程恢复后执行，@Async 方法自动继承租户与主体身份，
 * 解决 ThreadLocal 跨线程丢失问题。</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 默认 @Async 执行器（bean 名 taskExecutor，Spring 自动选用）。
     */
    @Override
    @Bean(name = "asyncExecutor")
    public AsyncTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("xcms-async-");
        // 队列满时由调用线程执行，天然背压，不丢任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 关键：跨线程传播 ActorContext（tenantId + principal）
        executor.setTaskDecorator(new ActorContextTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
