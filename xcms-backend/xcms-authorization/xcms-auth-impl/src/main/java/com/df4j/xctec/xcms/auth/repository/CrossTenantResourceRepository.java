package com.df4j.xctec.xcms.auth.repository;

import com.df4j.xctec.xcms.auth.domain.CrossTenantResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrossTenantResourceRepository extends JpaRepository<CrossTenantResource, Long>,
        JpaSpecificationExecutor<CrossTenantResource> {

    List<CrossTenantResource> findByTenantId(Long tenantId);

    boolean existsByTenantIdAndResourceKey(Long tenantId, String resourceKey);
}
