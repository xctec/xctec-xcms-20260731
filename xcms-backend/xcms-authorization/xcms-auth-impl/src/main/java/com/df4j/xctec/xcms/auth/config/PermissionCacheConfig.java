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
 * 且权限数据变更频率低，因此使用 Caffeine 本地缓存（默认 10 分钟过期）。</p>
 */
@Configuration
@EnableCaching
public class PermissionCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("userPermissions");
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000));
        return manager;
    }
}
