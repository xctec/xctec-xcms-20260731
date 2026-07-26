package com.df4j.xctec.xcms.identity.repository;

import com.df4j.xctec.xcms.identity.domain.SsoProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SsoProviderRepository extends JpaRepository<SsoProvider, Long> {

    Optional<SsoProvider> findByServerCodeAndEnabledTrue(String serverCode, boolean enabled);

    List<SsoProvider> findByTenantId(Long tenantId);

    boolean existsByTenantIdAndServerCode(Long tenantId, String serverCode);
}
