package com.df4j.xctec.xcms.app.config;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 种子数据初始化入口：H2 文件库首次启动时创建默认租户与管理员账号，确保开箱即用可登录。
 *
 * <p>仅在 {@code h2} profile 下生效。本类<b>非事务</b>，仅做编排：
 * <ol>
 *   <li>调用 {@link SeedDataService#ensureDefaultTenant()}（独立事务）建默认租户，拿到租户ID；</li>
 *   <li>{@link TenantContext#set(Long)} 切换到默认租户上下文；</li>
 *   <li>调用 {@link SeedDataService#seedAdmin(Long)}（独立事务）建管理员/角色/授权——
 *       此时事务开启 Hibernate 会话即捕获到正确租户，避免 {@code @TenantId} 校验报错。</li>
 * </ol>
 * 调用方非事务、被调方法经 Spring 代理生效事务，规避同类自调用导致 {@code @Transactional} 失效。
 */
@Slf4j
@Component
@Profile("h2")
@RequiredArgsConstructor
public class SeedDataRunner implements CommandLineRunner {

    private final SeedDataService seedDataService;

    @Override
    public void run(String... args) {
        Long tenantId = seedDataService.ensureDefaultTenant();
        try {
            TenantContext.set(tenantId);
            seedDataService.seedAdmin(tenantId);
        } finally {
            TenantContext.clear();
        }
    }
}
