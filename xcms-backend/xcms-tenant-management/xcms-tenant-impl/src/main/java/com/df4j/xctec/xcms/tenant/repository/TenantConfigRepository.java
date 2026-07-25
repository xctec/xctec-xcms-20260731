package com.df4j.xctec.xcms.tenant.repository;

import com.df4j.xctec.xcms.tenant.domain.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 租户配置仓储
 */
public interface TenantConfigRepository extends JpaRepository<TenantConfig, Long> {

    List<TenantConfig> findByTenantId(Long tenantId);

    Optional<TenantConfig> findByTenantIdAndConfigKey(Long tenantId, String configKey);
}
