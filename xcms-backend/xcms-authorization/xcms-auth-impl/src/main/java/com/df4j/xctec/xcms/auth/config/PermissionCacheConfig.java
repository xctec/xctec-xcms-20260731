package com.df4j.xctec.xcms.auth.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 权限缓存配置。
 *
 * <p>getUserPermissions 涉及「角色 -> 角色权限 -> 权限」多表查询，每次请求都重新计算会产生 N+1，
 * 且权限数据变更频率低，因此使用 Caffeine 本地缓存。</p>
 *
 * <ul>
 *   <li>TTL 5 分钟：权限变更事件（PermissionChangedEvent）会主动失效缓存，
 *       TTL 作为兜底防止事件丢失导致的脏读窗口。5min 在一致性与命中率间平衡。</li>
 *   <li>maximumSize 1000：W-TinyLFU 淘汰冷数据。</li>
 *   <li>recordStats：开启命中率统计，配合 actuator/metrics/cache 观察。</li>
 *   <li>setAllowNullValues(false)：禁止缓存 null/空值，防止无权限用户被缓存穿透。</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class PermissionCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("userPermissions");
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats());
        manager.setAllowNullValues(false);
        return manager;
    }
}
