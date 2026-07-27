package com.df4j.xctec.xcms.datapermission.cache;

/**
 * 数据权限规则缓存端口（AT-16）。
 *
 * <p>单体形态默认 Caffeine 实现；拆分后可切换 Redis 实现
 * （通过 {@code xcms.data-permission.cache.type} 配置切换）。</p>
 */
public interface CachePort {

    /** 读取缓存，未命中返回 null */
    <T> T get(String key, Class<T> type);

    /** 写入缓存（TTL 由实现决定） */
    void put(String key, Object value);

    /** 失效指定键 */
    void evict(String key);

    /** 按前缀批量失效（规则变更时清理某租户/资源的上下文缓存） */
    void evictByPrefix(String keyPrefix);
}
