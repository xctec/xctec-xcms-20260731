package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.api.event.PermissionChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 权限变更缓存失效监听器。
 *
 * <p>角色权限发生变更并成功提交后，清除 userPermissions 缓存，避免脏读。
 * 由于 PermissionChangedEvent 中的 userId 实际承载的是 roleId（角色级变更），
 * 无法精确映射到具体用户，故采用「整段失效（allEntries）」策略，
 * 牺牲少量缓存命中率换取一致性正确性。</p>
 */
@Slf4j
@Component
public class PermissionCacheEvictor {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public void onPermissionChanged(PermissionChangedEvent event) {
        log.debug("权限变更事件已提交，清除 userPermissions 缓存, tenantId={}, changeType={}",
                event.getTenantId(), event.getChangeType());
    }
}
