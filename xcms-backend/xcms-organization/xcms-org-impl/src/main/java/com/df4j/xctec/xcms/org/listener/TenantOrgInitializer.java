package com.df4j.xctec.xcms.org.listener;

import com.df4j.xctec.xcms.kernel.event.DomainEventListener;
import com.df4j.xctec.xcms.org.domain.Department;
import com.df4j.xctec.xcms.org.repository.DepartmentRepository;
import com.df4j.xctec.xcms.tenant.api.TenantInitStatusService;
import com.df4j.xctec.xcms.tenant.api.event.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 监听租户创建事件，初始化根部门。
 *
 * <p>经统一分发器调度，租户上下文切换由分发器按事件 tenantId 统一完成，
 * 本监听器内不再手动 {@code switchTo}；REQUIRES_NEW 新事务由分发器开启，
 * 新会话在租户切换后创建，{@code @TenantId} 捕获新租户。幂等查询显式带
 * tenantId，不依赖隐式 {@code @TenantId} 过滤（ADR-015）。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantOrgInitializer implements DomainEventListener<TenantCreatedEvent> {

    private final DepartmentRepository departmentRepository;
    private final TenantInitStatusService tenantInitStatusService;

    @Override
    @Transactional
    public void onEvent(TenantCreatedEvent event) {
        Long tenantId = event.getTenantId();
        if (tenantId == null) {
            log.warn("TenantCreatedEvent 缺少 tenantId，跳过组织初始化");
            return;
        }
        try {
            doInitialize(event, tenantId);
            tenantInitStatusService.record(tenantId, TenantInitStatusService.MODULE_ORG, true, null);
        } catch (Exception ex) {
            log.error("租户 {} 组织初始化失败: {}", tenantId, ex.getMessage(), ex);
            // 独立事务记录失败状态，供管理面补偿重试（ADR-015）
            tenantInitStatusService.record(tenantId, TenantInitStatusService.MODULE_ORG, false, ex.getMessage());
            throw ex;
        }
    }

    private void doInitialize(TenantCreatedEvent event, Long tenantId) {
        if (departmentRepository.findByTenantIdAndParentIdIsNullAndDeletedAtIsNull(tenantId).isEmpty()) {
            Department root = Department.builder()
                    .deptCode("ROOT")
                    .deptName(event.getTenantName() != null ? event.getTenantName() : "组织根")
                    .parentId(null)
                    .level(1)
                    .sortOrder(0)
                    .status("ACTIVE")
                    .build();
            root = departmentRepository.save(root);
            root.setPath("/" + root.getId());
            departmentRepository.save(root);
            log.info("租户 {} 根部门初始化完成", tenantId);
        }
    }

    @Override
    public Class<TenantCreatedEvent> eventType() {
        return TenantCreatedEvent.class;
    }
}
