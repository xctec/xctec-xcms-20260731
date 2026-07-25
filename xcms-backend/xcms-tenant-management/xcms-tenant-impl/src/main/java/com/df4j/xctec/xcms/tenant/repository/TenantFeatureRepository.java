package com.df4j.xctec.xcms.tenant.repository;

import com.df4j.xctec.xcms.tenant.domain.TenantFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 租户功能开关仓储
 */
public interface TenantFeatureRepository extends JpaRepository<TenantFeature, Long> {

    List<TenantFeature> findByTenantId(Long tenantId);

    Optional<TenantFeature> findByTenantIdAndFeatureCode(Long tenantId, String featureCode);
}
