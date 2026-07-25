package com.df4j.xctec.xcms.tenant.repository;

import com.df4j.xctec.xcms.tenant.domain.TenantRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 租户关系仓储
 */
public interface TenantRelationRepository extends JpaRepository<TenantRelation, Long> {

    List<TenantRelation> findByProjectTenantId(Long projectTenantId);

    List<TenantRelation> findByMemberTenantId(Long memberTenantId);
}
