package com.df4j.xctec.xcms.task.engine;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.message.api.MessageService;
import com.df4j.xctec.xcms.message.api.dto.request.SendMessageCommand;
import com.df4j.xctec.xcms.task.api.TaskHandler;
import com.df4j.xctec.xcms.task.domain.TaskAsync;
import com.df4j.xctec.xcms.task.domain.TaskExecutionLog;
import com.df4j.xctec.xcms.task.domain.TaskSchedule;
import com.df4j.xctec.xcms.task.repository.TaskAsyncRepository;
import com.df4j.xctec.xcms.task.repository.TaskExecutionLogRepository;
import com.df4j.xctec.xcms.task.repository.TaskScheduleRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 定时任务调度引擎。
 * <ul>
 *   <li>集群选主：仅 Leader 节点注册并执行定时任务（基于 DB 行锁 task_lock）。</li>
 *   <li>动态注册/反注册：启动或任务启停时按需 schedule/cancel。</li>
 *   <li>调度类型：CRON / FIXED_RATE / FIXED_DELAY，分别对应 CronTrigger / 固定速率 / 固定延迟。</li>
 *   <li>失败重试：从 handler_params 的 maxRetries / retryIntervalMs 读取策略，超时由 timeoutMs 控制。</li>
 *   <li>执行日志：每次执行记录 task_execution_log，含重试次数与错误信息。</li>
 *   <li>异步任务：所有节点轮询 task_async 中到期 PENDING 任务，通过原子状态切换领取并执行。</li>
 *   <li>失败告警：通过 MessageService 发送 NOTICE 类型消息。</li>
 * </ul>
 */
@Slf4j
@Service
public class TaskSchedulerEngine {

    private static final String LEADER_KEY = "scheduler";
    private static final long LEASE_SECONDS = 30;
    private static final long LEADERSHIP_CHECK_MS = 10_000;
    private static final long ASYNC_POLL_MS = 2_000;
    private static final int POOL_SIZE = 10;

    private final TaskLockService lockService;
    private final TaskScheduleRepository scheduleRepository;
    private final TaskExecutionLogRepository logRepository;
    private final TaskAsyncRepository asyncRepository;
    private final ObjectProvider<TaskHandler> handlers;
    private final ObjectProvider<MessageService> messageService;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<Long, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean leader = false;

    public TaskSchedulerEngine(TaskLockService lockService,
                               TaskScheduleRepository scheduleRepository,
                               TaskExecutionLogRepository logRepository,
                               TaskAsyncRepository asyncRepository,
                               ObjectProvider<TaskHandler> handlers,
                               ObjectProvider<MessageService> messageService) {
        this.lockService = lockService;
        this.scheduleRepository = scheduleRepository;
        this.logRepository = logRepository;
        this.asyncRepository = asyncRepository;
        this.handlers = handlers;
        this.messageService = messageService;
    }

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(POOL_SIZE);
        scheduler.setThreadNamePrefix("xcms-task-");
        scheduler.initialize();

        PeriodicTrigger check = new PeriodicTrigger(LEADERSHIP_CHECK_MS);
        check.setInitialDelay(LEADERSHIP_CHECK_MS);
        scheduler.schedule(this::leadershipTick, check);

        PeriodicTrigger poll = new PeriodicTrigger(ASYNC_POLL_MS);
        poll.setFixedRate(true);
        poll.setInitialDelay(ASYNC_POLL_MS);
        scheduler.schedule(this::pollAsync, poll);

        log.info("[task] scheduler engine started, instance={}", lockService.getInstanceId());
    }

    @PreDestroy
    public void destroy() {
        unscheduleAll();
        lockService.release();
        scheduler.destroy();
    }

    private void leadershipTick() {
        boolean acquired = lockService.tryAcquire(LEASE_SECONDS);
        if (acquired && !leader) {
            leader = true;
            scheduleAllEnabled();
        } else if (!acquired && leader) {
            leader = false;
            unscheduleAll();
        } else if (acquired && leader) {
            lockService.renew(LEASE_SECONDS);
        }
    }

    public void scheduleAllEnabled() {
        unscheduleAll();
        for (TaskSchedule s : scheduleRepository.findByStatus("ENABLED")) {
            scheduleTask(s);
        }
        log.info("[task] scheduled {} enabled tasks", futures.size());
    }

    public void scheduleTask(TaskSchedule s) {
        Trigger trigger = buildTrigger(s);
        if (trigger == null) {
            log.warn("[task] skip schedule task={} due to invalid trigger", s.getTaskCode());
            return;
        }
        if (!"ENABLED".equals(s.getStatus())) {
            return;
        }
        ScheduledFuture<?> f = scheduler.schedule(() -> runTask(s.getId()), trigger);
        futures.put(s.getId(), f);
    }

    public void unscheduleOne(Long taskId) {
        ScheduledFuture<?> f = futures.remove(taskId);
        if (f != null && !f.isDone()) {
            f.cancel(false);
        }
    }

    public void unscheduleAll() {
        for (ScheduledFuture<?> f : futures.values()) {
            if (f != null && !f.isDone()) {
                f.cancel(false);
            }
        }
        futures.clear();
    }

    /** 立即触发一次执行（异步，不受 Leader 限制） */
    public void triggerNow(Long taskId) {
        scheduler.schedule(() -> runTask(taskId), Instant.now());
    }

    private void pollAsync() {
        List<TaskAsync> due = asyncRepository.findDuePending(LocalDateTime.now());
        for (TaskAsync t : due) {
            if (asyncRepository.updateStatusIfExpected(t.getId(), "RUNNING", "PENDING") == 1) {
                scheduler.schedule(() -> runAsync(t.getId()), Instant.now());
            }
        }
    }

    private void runAsync(Long id) {
        TaskAsync t = asyncRepository.findById(id).orElse(null);
        if (t == null || !"RUNNING".equals(t.getStatus())) {
            return;
        }
        TaskHandler handler = resolveHandler(t.getTaskType());
        Map<String, Object> params = parseParams(t.getPayload());
        Long tenantId = t.getTenantId();
        TenantContext.TenantInfo original = tenantId != null ? TenantContext.switchTo(tenantId) : null;
        try {
            if (handler != null) {
                safeExecute(handler, params);
                t.setStatus("SUCCESS");
            } else {
                t.setStatus("FAILED");
                t.setErrorMsg("no handler for taskType=" + t.getTaskType());
            }
            t.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            t.setStatus("FAILED");
            t.setErrorMsg(truncate(e.getMessage(), 2000));
        } finally {
            TenantContext.restore(original);
        }
        asyncRepository.save(t);
    }

    private Trigger buildTrigger(TaskSchedule s) {
        if ("CRON".equals(s.getTaskType())) {
            try {
                return new CronTrigger(s.getCronExpression());
            } catch (Exception e) {
                log.error("[task] invalid cron for task={}: {}", s.getTaskCode(), s.getCronExpression(), e);
                return null;
            }
        } else if ("FIXED_RATE".equals(s.getTaskType())) {
            PeriodicTrigger t = new PeriodicTrigger(s.getFixedRate() == null ? 60000 : s.getFixedRate());
            t.setFixedRate(true);
            return t;
        } else if ("FIXED_DELAY".equals(s.getTaskType())) {
            return new PeriodicTrigger(s.getFixedRate() == null ? 60000 : s.getFixedRate());
        }
        return null;
    }

    private void runTask(Long scheduleId) {
        TaskSchedule s = scheduleRepository.findById(scheduleId).orElse(null);
        if (s == null) {
            unscheduleOne(scheduleId);
            return;
        }
        if (!"ENABLED".equals(s.getStatus())) {
            unscheduleOne(scheduleId);
            return;
        }
        TaskHandler handler = resolveHandler(s.getHandlerName());
        if (handler == null) {
            log.warn("[task] no handler bean found for name={}, task={}", s.getHandlerName(), s.getTaskCode());
            return;
        }
        Map<String, Object> params = parseParams(s.getHandlerParams());
        int maxRetries = intOf(params.get("maxRetries"), 0);
        long timeoutMs = longOf(params.get("timeoutMs"), 0L);
        long retryIntervalMs = longOf(params.get("retryIntervalMs"), 60_000L);
        Long tenantId = s.getTenantId();
        TenantContext.TenantInfo original = tenantId != null ? TenantContext.switchTo(tenantId) : null;
        try {
            runWithRetry(s, handler, params, 0, maxRetries, timeoutMs, retryIntervalMs);
        } finally {
            TenantContext.restore(original);
        }
    }

    private void runWithRetry(TaskSchedule s, TaskHandler handler, Map<String, Object> params,
                              int attempt, int maxRetries, long timeoutMs, long retryIntervalMs) {
        TaskExecutionLog log = new TaskExecutionLog();
        log.setTenantId(s.getTenantId());
        log.setTaskId(s.getId());
        log.setTaskName(s.getTaskName());
        log.setStartTime(LocalDateTime.now());
        log.setStatus(attempt == 0 ? "RUNNING" : "RETRYING");
        log.setRetryCount(attempt);
        log = logRepository.save(log);

        try {
            if (timeoutMs > 0) {
                CompletableFuture<Void> f = CompletableFuture.runAsync(() -> safeExecute(handler, params));
                try {
                    f.get(timeoutMs, TimeUnit.MILLISECONDS);
                } catch (TimeoutException te) {
                    f.cancel(true);
                    throw new RuntimeException("任务执行超时(" + timeoutMs + "ms)");
                } catch (InterruptedException | ExecutionException ie) {
                    throw new RuntimeException(ie.getMessage(), ie);
                }
            } else {
                safeExecute(handler, params);
            }
            log.setEndTime(LocalDateTime.now());
            log.setStatus("SUCCESS");
            s.setLastExecAt(LocalDateTime.now());
            s.setNextExecAt(nextExecTime(s));
            scheduleRepository.save(s);
        } catch (Exception e) {
            log.setEndTime(LocalDateTime.now());
            log.setErrorMsg(truncate(e.getMessage(), 2000));
            if (attempt < maxRetries) {
                log.setStatus("RETRYING");
                logRepository.save(log);
                scheduler.schedule(() -> runWithRetry(s, handler, params, attempt + 1,
                        maxRetries, timeoutMs, retryIntervalMs), Instant.now().plusMillis(retryIntervalMs));
                return;
            }
            log.setStatus("FAILED");
            alert(s, e);
        }
        logRepository.save(log);
    }

    private void safeExecute(TaskHandler handler, Map<String, Object> params) {
        try {
            handler.execute(params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TaskHandler resolveHandler(String name) {
        return handlers.orderedStream()
                .filter(h -> name != null && name.equals(h.getHandlerName()))
                .findFirst()
                .orElse(null);
    }

    private void alert(TaskSchedule s, Exception e) {
        MessageService svc = messageService.getIfAvailable();
        if (svc == null) {
            log.warn("[task] task failed and no MessageService available for alert: {}", s.getTaskCode());
            return;
        }
        try {
            SendMessageCommand cmd = new SendMessageCommand();
            cmd.setMsgType("NOTICE");
            cmd.setTitle("定时任务执行失败: " + s.getTaskName());
            cmd.setContent("任务[" + s.getTaskName() + "/" + s.getTaskCode() + "]执行失败: "
                    + truncate(e.getMessage(), 500));
            cmd.setRecipientIds(new ArrayList<>());
            svc.send(cmd);
        } catch (Exception ex) {
            log.warn("[task] send alert failed", ex);
        }
    }

    private LocalDateTime nextExecTime(TaskSchedule s) {
        if ("CRON".equals(s.getTaskType()) && s.getCronExpression() != null) {
            try {
                return CronExpression.parse(s.getCronExpression()).next(LocalDateTime.now());
            } catch (Exception e) {
                return null;
            }
        } else if (s.getFixedRate() != null) {
            return LocalDateTime.now().plus(Duration.ofMillis(s.getFixedRate().longValue()));
        }
        return null;
    }

    private Map<String, Object> parseParams(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("[task] parse handler_params failed: {}", truncate(json, 200));
            return new HashMap<>();
        }
    }

    private int intOf(Object o, int def) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        if (o instanceof String s && !s.isBlank()) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    private long longOf(Object o, long def) {
        if (o instanceof Number n) {
            return n.longValue();
        }
        if (o instanceof String s && !s.isBlank()) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
