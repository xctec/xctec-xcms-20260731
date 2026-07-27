package com.df4j.xctec.xcms.kernel.event;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link EventDedupPort} 的进程内默认实现。
 *
 * <p>基于 ConcurrentHashMap 记录已处理的（监听器, 事件）键，带 TTL 惰性清理防止无界增长。
 * 仅适用于单体形态；多实例/MQ 场景应替换为 Redis/数据库实现。</p>
 */
public class InMemoryEventDedup implements EventDedupPort {

    /** 默认保留 24 小时 */
    private static final long DEFAULT_TTL_MILLIS = 24L * 60 * 60 * 1000;
    /** 超过该容量时触发一次过期清理 */
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<String, Long> processed = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public InMemoryEventDedup() {
        this(DEFAULT_TTL_MILLIS);
    }

    public InMemoryEventDedup(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    @Override
    public boolean tryMarkProcessed(String listenerName, String eventId) {
        cleanupIfNeeded();
        String key = listenerName + '#' + eventId;
        return processed.putIfAbsent(key, System.currentTimeMillis()) == null;
    }

    private void cleanupIfNeeded() {
        if (processed.size() < CLEANUP_THRESHOLD) {
            return;
        }
        long expireBefore = System.currentTimeMillis() - ttlMillis;
        Iterator<Map.Entry<String, Long>> it = processed.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() < expireBefore) {
                it.remove();
            }
        }
    }
}
