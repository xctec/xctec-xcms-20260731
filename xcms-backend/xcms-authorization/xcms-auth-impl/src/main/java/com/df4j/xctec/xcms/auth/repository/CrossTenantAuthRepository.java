package com.df4j.xctec.xcms.auth.repository;

import com.df4j.xctec.xcms.auth.domain.CrossTenantAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CrossTenantAuthRepository extends JpaRepository<CrossTenantAuth, Long> {

    List<CrossTenantAuth> findByUserIdAndTargetTenantIdAndStatus(Long userId, Long targetTenantId, String status);

    @Query("""
        select a from CrossTenantAuth a
        where (:tenantId is null or a.tenantId = :tenantId)
          and (:targetTenantId is null or a.targetTenantId = :targetTenantId)
          and (:userId is null or a.userId = :userId)
          and (:status is null or a.status = :status)
        """)
    List<CrossTenantAuth> search(@Param("tenantId") Long tenantId,
                                 @Param("targetTenantId") Long targetTenantId,
                                 @Param("userId") Long userId,
                                 @Param("status") String status);

    @Query("""
        select a from CrossTenantAuth a
        where (:tenantId is null or a.tenantId = :tenantId)
          and (:targetTenantId is null or a.targetTenantId = :targetTenantId)
          and (:userId is null or a.userId = :userId)
          and (:status is null or a.status = :status)
        """)
    Page<CrossTenantAuth> searchPage(@Param("tenantId") Long tenantId,
                                     @Param("targetTenantId") Long targetTenantId,
                                     @Param("userId") Long userId,
                                     @Param("status") String status,
                                     Pageable pageable);
}
