package com.df4j.xctec.xcms.auth.repository;

import com.df4j.xctec.xcms.auth.domain.ColumnMask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColumnMaskRepository extends JpaRepository<ColumnMask, Long> {

    List<ColumnMask> findByResourceTypeAndTenantId(String resourceType, Long tenantId);
}
