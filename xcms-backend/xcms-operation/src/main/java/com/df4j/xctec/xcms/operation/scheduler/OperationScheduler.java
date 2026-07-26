package com.df4j.xctec.xcms.operation.scheduler;

import com.df4j.xctec.xcms.operation.api.AlertRuleService;
import com.df4j.xctec.xcms.operation.service.HealthCheckServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;

/**
 * 运维调度器：周期执行健康检查的到期探测与告警规则评估。
 * 健康检查与告警评估属于运维动作，在多节点下可由任一节点执行（数据以 DB 为准，天然幂等）。
 */
@Slf4j
@Component
public class OperationScheduler {

    private static final long HEALTH_INTERVAL_MS = 30_000;
    private static final long ALERT_INTERVAL_MS = 60_000;

    private final HealthCheckServiceImpl healthCheckService;
    private final AlertRuleService alertRuleService;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    public OperationScheduler(HealthCheckServiceImpl healthCheckService, AlertRuleService alertRuleService) {
        this.healthCheckService = healthCheckService;
        this.alertRuleService = alertRuleService;
    }

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("xcms-ops-");
        scheduler.initialize();

        PeriodicTrigger health = new PeriodicTrigger(HEALTH_INTERVAL_MS);
        health.setFixedRate(true);
        health.setInitialDelay(10_000);
        scheduler.schedule(healthCheckService::runDueChecks, health);

        PeriodicTrigger alert = new PeriodicTrigger(ALERT_INTERVAL_MS);
        alert.setFixedRate(true);
        alert.setInitialDelay(15_000);
        scheduler.schedule(alertRuleService::evaluate, alert);

        log.info("[ops] operation scheduler started");
    }

    @PreDestroy
    public void destroy() {
        scheduler.destroy();
    }
}
