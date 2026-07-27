package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.api.event.PermissionChangedEvent;
import com.df4j.xctec.xcms.kernel.event.DomainEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

/**
 * 权限变更缓存失效监听器。
 *
 * <p>角色权限发生变更并成功提交后，清除 userPermissions 缓存，避免脏读。
 * 由于 PermissionChangedEvent 中的 userId 实际承载的是 roleId（角色级变更），
 * 无法精确映射到具体用户，故采用「整段失效（allEntries）」策略，
 * 牺牲少量缓存命中率换取一致性正确性。</p>
 *
 * <p>经统一分发器调度；AFTER_COMMIT 语义由分发器入口（进程内桥接）统一保证。</p>
 */
@Slf4j
@Component
public class PermissionCacheEvictor implements DomainEventListener<PermissionChangedEvent> {

    @Override
    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public void onEvent(PermissionChangedEvent event) {
        log.debug("权限变更事件已提交，清除 userPermissions 缓存, tenantId={}, changeType={}",
                event.getTenantId(), event.getChangeType());
    }

    @Override
    public Class<PermissionChangedEvent> eventType() {
        return PermissionChangedEvent.class;
    }
}
