package com.df4j.xctec.xcms.tenant.repository;

import com.df4j.xctec.xcms.tenant.domain.TenantInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 租户信息仓储
 */
public interface TenantInfoRepository extends JpaRepository<TenantInfo, Long>,
        JpaSpecificationExecutor<TenantInfo> {

    Optional<TenantInfo> findByTenantCode(String tenantCode);

    List<TenantInfo> findByParentId(Long parentId);

    List<TenantInfo> findByDeletedAtIsNull();

    /**
     * 查询租户子树（path 以指定租户 path 为前缀的全部未删除租户）
     */
    @Query("SELECT t FROM TenantInfo t WHERE t.deletedAt IS NULL AND t.path LIKE :pathPrefix")
    List<TenantInfo> findSubTree(@Param("pathPrefix") String pathPrefix);

    /**
     * 根据编码（忽略已删除）查询，用于唯一性校验
     */
    boolean existsByTenantCodeAndDeletedAtIsNull(String tenantCode);
}
