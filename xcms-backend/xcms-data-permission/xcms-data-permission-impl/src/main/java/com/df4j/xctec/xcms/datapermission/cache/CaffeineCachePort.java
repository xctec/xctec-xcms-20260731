package com.df4j.xctec.xcms.datapermission.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 单体形态默认缓存实现（AT-16）：进程内 Caffeine，短 TTL。
 *
 * <p>集群/拆分形态将 {@code xcms.data-permission.cache.type} 切为 {@code redis}
 * 并提供对应 CachePort 实现即可，业务代码零改动。</p>
 */
@Component
@ConditionalOnProperty(name = "xcms.data-permission.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class CaffeineCachePort implements CachePort {

    private final Cache<String, Object> cache;

    public CaffeineCachePort(
            @Value("${xcms.data-permission.cache.ttl-seconds:60}") long ttlSeconds,
            @Value("${xcms.data-permission.cache.max-size:10000}") long maxSize) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .maximumSize(maxSize)
                .build();
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        Object value = cache.getIfPresent(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    @Override
    public void put(String key, Object value) {
        if (value != null) {
            cache.put(key, value);
        }
    }

    @Override
    public void evict(String key) {
        cache.invalidate(key);
    }

    @Override
    public void evictByPrefix(String keyPrefix) {
        cache.asMap().keySet().removeIf(k -> k.startsWith(keyPrefix));
    }
}
