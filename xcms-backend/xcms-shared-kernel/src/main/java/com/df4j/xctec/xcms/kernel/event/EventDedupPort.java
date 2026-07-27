package com.df4j.xctec.xcms.kernel.event;

/**
 * 事件消费幂等去重端口。
 *
 * <p>at-least-once 投递语义下事件可能重复送达，分发器在调用监听器前先经本端口判定：
 * 首次处理放行，重复事件跳过，防止重复副作用（重复告警、重复初始化等）。</p>
 *
 * <p>单体形态默认内存实现（进程内事件基本不重复，主要为幂等语义占位）；
 * 拆分微服务接入 MQ 后替换为 Redis SETNX 或数据库唯一约束实现，端口不变（ADR-014）。</p>
 */
public interface EventDedupPort {

    /**
     * 尝试将（监听器, 事件）标记为已处理。
     *
     * @param listenerName 监听器标识（同一事件可被多个监听器各自消费一次）
     * @param eventId      事件唯一 ID
     * @return true 表示首次处理（放行）；false 表示已处理过（跳过）
     */
    boolean tryMarkProcessed(String listenerName, String eventId);
}
