package com.df4j.xctec.xcms.message.sse;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE 连接注册表（ADR-018 / AT-20）。
 *
 * <p>按 {@code tenantId:userId} 维护本地长连接（同一用户多标签页对应多个 emitter）。
 * 单体形态为进程内 Map；多实例扩展经 Redis pub/sub 广播（AT-22，按需）。</p>
 *
 * <p>心跳：内置调度器定期向所有连接发注释行（{@code :heartbeat}），防止代理/网关空闲超时断链。
 * 断线清理：emitter 完成/超时/出错时自动从注册表移除。</p>
 */
@Slf4j
@Component
public class SseConnectionRegistry {

    private static final long HEARTBEAT_INTERVAL_SECONDS = 30;

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> connections = new ConcurrentHashMap<>();

    private final ScheduledExecutorService heartbeatScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sse-heartbeat");
                t.setDaemon(true);
                return t;
            });

    public SseConnectionRegistry() {
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeat,
                HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /** 注册一个长连接（超时 0 = 不超时，由心跳与断线回调管理生命周期） */
    public SseEmitter register(Long tenantId, Long userId) {
        String key = key(tenantId, userId);
        SseEmitter emitter = new SseEmitter(0L);
        connections.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup = () -> remove(key, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> remove(key, emitter));
        log.debug("[sse] connected {}, connections={}", key, count(key));
        return emitter;
    }

    /** 向指定租户+用户的所有在线连接推送事件；无连接时静默返回 false */
    public boolean send(Long tenantId, Long userId, String eventName, Object data) {
        List<SseEmitter> emitters = connections.get(key(tenantId, userId));
        if (emitters == null || emitters.isEmpty()) {
            return false;
        }
        boolean delivered = false;
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
                delivered = true;
            } catch (IOException | IllegalStateException e) {
                // 发送失败视为断线，移除并结束该 emitter
                remove(key(tenantId, userId), emitter);
                emitter.completeWithError(e);
            }
        }
        return delivered;
    }

    /** 当前在线连接数（监控/测试用） */
    public int count(Long tenantId, Long userId) {
        return count(key(tenantId, userId));
    }

    private int count(String key) {
        List<SseEmitter> list = connections.get(key);
        return list == null ? 0 : list.size();
    }

    private void remove(String key, SseEmitter emitter) {
        connections.computeIfPresent(key, (k, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
        log.debug("[sse] disconnected {}, connections={}", key, count(key));
    }

    private void sendHeartbeat() {
        connections.forEach((key, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    // SSE 注释行，客户端 EventSource 忽略，仅用于保活
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (IOException | IllegalStateException e) {
                    remove(key, emitter);
                    emitter.completeWithError(e);
                }
            }
        });
    }

    private String key(Long tenantId, Long userId) {
        return tenantId + ":" + userId;
    }

    @PreDestroy
    public void shutdown() {
        heartbeatScheduler.shutdownNow();
        connections.values().forEach(list -> list.forEach(SseEmitter::complete));
        connections.clear();
    }
}
