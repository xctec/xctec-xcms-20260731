package com.df4j.xctec.xcms.tenant.repository;

import com.df4j.xctec.xcms.tenant.api.enums.QuotaType;
import com.df4j.xctec.xcms.tenant.domain.TenantQuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 租户配额仓储
 */
public interface TenantQuotaRepository extends JpaRepository<TenantQuota, Long> {

    List<TenantQuota> findByTenantId(Long tenantId);

    Optional<TenantQuota> findByTenantIdAndQuotaTypeAndPeriod(Long tenantId, QuotaType quotaType, String period);
}
