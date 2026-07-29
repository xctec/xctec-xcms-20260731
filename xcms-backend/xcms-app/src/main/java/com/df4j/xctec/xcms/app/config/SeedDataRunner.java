package com.df4j.xctec.xcms.app.config;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;
import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import com.df4j.xctec.xcms.tenant.api.event.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 平台种子数据初始化入口（引导数据，非演示数据）。
 *
 * <p>与演示/样例数据不同，平台种子（默认租户 + 平台管理员 + 操作权限码 + 管理员角色）
 * 是系统运行的基础数据，<b>任何环境</b>（h2 / mysql / prod）首次启动都需就绪，故不再
 * 绑定 {@code h2} profile，改为经 {@code xcms.seed.bootstrap-enabled} 开关控制
 * （默认开启，可显式关闭以跳过引导）。全程幂等，重复启动不会重复插入或覆盖。
 *
 * <p>编排（本类<b>非事务</b>，仅做编排）：
 * <ol>
 *   <li>调用 {@link SeedDataService#ensureDefaultTenant()}（独立事务）建默认租户，拿到租户ID；</li>
 *   <li>{@link TenantContext#set(Long)} 切换到默认租户上下文；</li>
 *   <li>调用 {@link SeedDataService#seedAdmin(Long)}（独立事务）建管理员/角色/授权；</li>
 *   <li>默认租户建好后发布 {@link TenantCreatedEvent}，使其<b>走与正常租户一致的事件初始化路径</b>，
 *       由 {@code TenantUserInitializer}/{@code TenantOrgInitializer} 补齐 {@code tenant_user} 角色与
 *       ROOT 根部门（设计 §5.1 缺口），同时因 admin 已存在而跳过重复创建。</li>
 * </ol>
 *
 * <p>演示/样例数据（如有）应放在独立 {@code @Profile({"h2","dev","local"})} 的 Runner 中，
 * 勿与本引导数据混用。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "xcms.seed", name = "bootstrap-enabled",
        havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class SeedDataRunner implements CommandLineRunner {

    private final SeedDataService seedDataService;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public void run(String... args) {
        Long tenantId = seedDataService.ensureDefaultTenant();
        try {
            TenantContext.set(tenantId);
            seedDataService.seedAdmin(tenantId);
        } finally {
            TenantContext.clear();
        }
        // 设计 §5.1：默认租户建好后再经事件路径补齐 tenant_user 角色与 ROOT 根部门。
        // 仅在缺口仍存在时发布，避免每次启动重复触发初始化与状态表写入；半初始化状态可自愈。
        if (seedDataService.defaultTenantNeedsEventInit()) {
            publishDefaultTenantCreatedEvent(tenantId);
        }
    }

    private void publishDefaultTenantCreatedEvent(Long tenantId) {
        TenantCreatedEvent event = new TenantCreatedEvent();
        event.setTenantId(tenantId);
        // 与 SeedDataService.DEFAULT_TENANT_CODE / DEFAULT_TENANT_NAME 保持一致
        event.setTenantCode("default");
        event.setTenantName("默认租户");
        event.setTenantType(TenantType.PLATFORM);
        event.setParentId(null);
        domainEventPublisher.publish(event);
        log.info("[Seed] 已发布默认租户创建事件，触发 tenant_user 角色与 ROOT 部门初始化（§5.1）");
    }
}
