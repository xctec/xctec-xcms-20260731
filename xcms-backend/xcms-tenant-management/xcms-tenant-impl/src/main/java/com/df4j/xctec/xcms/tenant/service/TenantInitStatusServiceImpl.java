package com.df4j.xctec.xcms.tenant.service;

import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;
import com.df4j.xctec.xcms.kernel.exception.NotFoundException;
import com.df4j.xctec.xcms.tenant.api.TenantInitStatusService;
import com.df4j.xctec.xcms.tenant.api.dto.TenantInitStatusDTO;
import com.df4j.xctec.xcms.tenant.api.event.TenantCreatedEvent;
import com.df4j.xctec.xcms.tenant.domain.TenantInfo;
import com.df4j.xctec.xcms.tenant.domain.TenantInitStatus;
import com.df4j.xctec.xcms.tenant.repository.TenantInfoRepository;
import com.df4j.xctec.xcms.tenant.repository.TenantInitStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户初始化状态跟踪与补偿实现（ADR-015）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantInitStatusServiceImpl implements TenantInitStatusService {

    private final TenantInitStatusRepository initStatusRepository;
    private final TenantInfoRepository tenantInfoRepository;
    private final DomainEventPublisher eventPublisher;

    /**
     * REQUIRES_NEW：失败链路中监听器事务可能已 rollback-only，
     * 状态记录必须独立提交，否则失败信息本身也会丢失。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long tenantId, String module, boolean success, String errorMsg) {
        TenantInitStatus entity = initStatusRepository.findByTenantIdAndModule(tenantId, module)
                .orElseGet(() -> {
                    TenantInitStatus s = new TenantInitStatus();
                    s.setTenantId(tenantId);
                    s.setModule(module);
                    s.setRetryCount(0);
                    return s;
                });
        if (entity.getId() != null && STATUS_FAILED.equals(entity.getStatus())) {
            entity.setRetryCount(entity.getRetryCount() == null ? 1 : entity.getRetryCount() + 1);
        }
        entity.setStatus(success ? STATUS_SUCCESS : STATUS_FAILED);
        entity.setErrorMsg(success ? null : truncate(errorMsg));
        initStatusRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantInitStatusDTO> listFailed() {
        return initStatusRepository.findByStatusOrderByUpdatedAtDesc(STATUS_FAILED).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void retry(Long tenantId) {
        TenantInfo tenant = tenantInfoRepository.findById(tenantId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("租户", tenantId));
        log.info("触发租户 {} 初始化补偿，重发 TenantCreatedEvent", tenantId);
        TenantCreatedEvent event = new TenantCreatedEvent();
        event.setTenantId(tenant.getId());
        event.setTenantCode(tenant.getTenantCode());
        event.setTenantName(tenant.getTenantName());
        event.setTenantType(tenant.getTenantType());
        event.setParentId(tenant.getParentId());
        // 监听器幂等（按 tenantId 显式查询已存在则跳过），重发安全
        eventPublisher.publish(event);
    }

    private TenantInitStatusDTO toDto(TenantInitStatus entity) {
        TenantInitStatusDTO dto = new TenantInitStatusDTO();
        dto.setId(entity.getId());
        dto.setTenantId(entity.getTenantId());
        dto.setModule(entity.getModule());
        dto.setStatus(entity.getStatus());
        dto.setErrorMsg(entity.getErrorMsg());
        dto.setRetryCount(entity.getRetryCount());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private String truncate(String msg) {
        if (msg == null) {
            return null;
        }
        return msg.length() > 1000 ? msg.substring(0, 1000) : msg;
    }
}
