package com.df4j.xctec.xcms.tenant.service;

import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.tenant.api.TenantQuotaService;
import com.df4j.xctec.xcms.tenant.api.dto.QuotaAllocateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.QuotaUsageDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuotaDTO;
import com.df4j.xctec.xcms.tenant.api.enums.QuotaType;
import com.df4j.xctec.xcms.tenant.domain.TenantQuota;
import com.df4j.xctec.xcms.tenant.mapper.TenantMapper;
import com.df4j.xctec.xcms.tenant.repository.TenantQuotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 租户配额实现
 */
@Service
@RequiredArgsConstructor
public class TenantQuotaServiceImpl implements TenantQuotaService {

    private static final String DEFAULT_PERIOD = "TOTAL";

    private final TenantQuotaRepository tenantQuotaRepository;
    private final TenantMapper tenantMapper;

    @Override
    @Transactional
    public void allocateQuota(Long tenantId, QuotaAllocateRequest request) {
        String period = request.getPeriod() == null ? DEFAULT_PERIOD : request.getPeriod();
        Optional<TenantQuota> existing = tenantQuotaRepository
                .findByTenantIdAndQuotaTypeAndPeriod(tenantId, request.getQuotaType(), period);
        TenantQuota quota = existing.orElseGet(TenantQuota::new);
        quota.setTenantId(tenantId);
        quota.setQuotaType(request.getQuotaType());
        quota.setPeriod(period);
        quota.setQuotaLimit(request.getQuotaLimit());
        if (quota.getQuotaUsed() == null) {
            quota.setQuotaUsed(0L);
        }
        if (quota.getAllocatedTo() == null) {
            quota.setAllocatedTo(0L);
        }
        tenantQuotaRepository.save(quota);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantQuotaDTO> getQuotas(Long tenantId) {
        return tenantMapper.toQuotaDtoList(tenantQuotaRepository.findByTenantId(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkQuota(Long tenantId, QuotaType quotaType, long amount) {
        return tenantQuotaRepository
                .findByTenantIdAndQuotaTypeAndPeriod(tenantId, quotaType, DEFAULT_PERIOD)
                .map(q -> available(q) >= amount)
                .orElse(true);
    }

    @Override
    @Transactional
    public void consumeQuota(Long tenantId, QuotaType quotaType, long amount) {
        tenantQuotaRepository
                .findByTenantIdAndQuotaTypeAndPeriod(tenantId, quotaType, DEFAULT_PERIOD)
                .ifPresent(q -> {
                    q.setQuotaUsed(q.getQuotaUsed() + amount);
                    tenantQuotaRepository.save(q);
                });
    }

    @Override
    @Transactional
    public void releaseQuota(Long tenantId, QuotaType quotaType, long amount) {
        tenantQuotaRepository
                .findByTenantIdAndQuotaTypeAndPeriod(tenantId, quotaType, DEFAULT_PERIOD)
                .ifPresent(q -> {
                    q.setQuotaUsed(Math.max(0L, q.getQuotaUsed() - amount));
                    tenantQuotaRepository.save(q);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public QuotaUsageDTO getQuotaUsage(Long tenantId) {
        QuotaUsageDTO usage = new QuotaUsageDTO();
        usage.setTenantId(tenantId);
        List<TenantQuota> quotas = tenantQuotaRepository.findByTenantId(tenantId);
        usage.setItems(quotas.stream().map(tenantMapper::toUsageItem).toList());
        return usage;
    }

    private long available(TenantQuota quota) {
        if (quota.getQuotaLimit() == null) {
            return Long.MAX_VALUE;
        }
        long used = quota.getQuotaUsed() == null ? 0L : quota.getQuotaUsed();
        long allocated = quota.getAllocatedTo() == null ? 0L : quota.getAllocatedTo();
        return quota.getQuotaLimit() - used - allocated;
    }
}
