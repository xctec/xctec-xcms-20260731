package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.api.BusinessVisibilityAuthService;
import com.df4j.xctec.xcms.task.api.TaskHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 内置任务处理器：清理过期的跨租户授权记录（handler_name=CROSS_TENANT_AUTH_EXPIRE）。
 * 由定时任务模块按周期调用 BusinessVisibilityAuthService.cleanupExpiredAuthorizations()。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrossTenantAuthExpireHandler implements TaskHandler {

    private final BusinessVisibilityAuthService authService;

    @Override
    public String getHandlerName() {
        return "CROSS_TENANT_AUTH_EXPIRE";
    }

    @Override
    public String getGroup() {
        return "auth";
    }

    @Override
    public void execute(Map<String, Object> params) {
        int expired = authService.cleanupExpiredAuthorizations();
        log.info("[cross-tenant] expired authorizations cleaned: {}", expired);
    }
}
