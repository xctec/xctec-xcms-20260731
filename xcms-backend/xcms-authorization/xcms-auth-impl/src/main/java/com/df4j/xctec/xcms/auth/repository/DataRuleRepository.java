package com.df4j.xctec.xcms.auth.repository;

import com.df4j.xctec.xcms.auth.domain.DataRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataRuleRepository extends JpaRepository<DataRule, Long> {

    List<DataRule> findByResourceTypeAndTenantId(String resourceType, Long tenantId);

    List<DataRule> findByResourceTypeAndTenantIdAndStatus(String resourceType, Long tenantId, String status);

    List<DataRule> findByIdInAndTenantId(List<Long> ids, Long tenantId);
}
