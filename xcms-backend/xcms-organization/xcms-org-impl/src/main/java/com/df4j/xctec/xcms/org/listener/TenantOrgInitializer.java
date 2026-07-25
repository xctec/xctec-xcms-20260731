package com.df4j.xctec.xcms.org.listener;

import com.df4j.xctec.xcms.org.domain.Department;
import com.df4j.xctec.xcms.org.repository.DepartmentRepository;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.tenant.api.event.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 监听租户创建事件，初始化根部门
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantOrgInitializer {

    private final DepartmentRepository departmentRepository;

    @EventListener
    @Transactional
    public void onTenantCreated(TenantCreatedEvent event) {
        Long tenantId = event.getTenantId();
        TenantContext.TenantInfo original = TenantContext.switchTo(tenantId);
        try {
            if (departmentRepository.findByParentIdIsNullAndDeletedAtIsNull().isEmpty()) {
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
        } finally {
            TenantContext.restore(original);
        }
    }
}
