package com.df4j.xctec.xcms.tenant.repository;

import com.df4j.xctec.xcms.tenant.domain.TenantInitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantInitStatusRepository extends JpaRepository<TenantInitStatus, Long> {

    Optional<TenantInitStatus> findByTenantIdAndModule(Long tenantId, String module);

    List<TenantInitStatus> findByStatusOrderByUpdatedAtDesc(String status);
}
