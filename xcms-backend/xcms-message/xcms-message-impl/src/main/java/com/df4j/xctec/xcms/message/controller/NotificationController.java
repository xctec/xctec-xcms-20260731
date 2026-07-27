package com.df4j.xctec.xcms.message.controller;

import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.message.sse.SseConnectionRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 通知推送（ADR-018 / AT-20）。
 *
 * <p>SSE 长连接端点：浏览器 EventSource 仅支持 GET，故本端点例外于全 POST 风格。
 * 连接按租户+用户注册到 {@link SseConnectionRegistry}，推送由事件监听器驱动（AT-21）。</p>
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "通知 Notification", description = "业务面：SSE 实时通知推送")
@RequiredArgsConstructor
public class NotificationController {

    private final SseConnectionRegistry registry;

    @Operation(summary = "订阅通知推送", description = "SSE 长连接（EventSource），按当前登录用户（租户+用户）订阅；心跳保活，断线由客户端重连。")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getCurrentUserId();
        if (tenantId == null || userId == null) {
            throw new BusinessException(ErrorCodes.UNAUTHORIZED, "SSE 订阅需要登录态");
        }
        return registry.register(tenantId, userId);
    }
}
