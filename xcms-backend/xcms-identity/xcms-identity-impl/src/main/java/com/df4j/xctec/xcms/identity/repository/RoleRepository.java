package com.df4j.xctec.xcms.identity.repository;

import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.identity.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleCode(String roleCode);

    /** 显式带 tenantId 的查询：租户初始化等跨租户场景使用，不依赖隐式 @TenantId 过滤 */
    Optional<Role> findByTenantIdAndRoleCode(Long tenantId, String roleCode);

    boolean existsByRoleCode(String roleCode);

    List<Role> findByRoleScope(RoleScope roleScope);

    @Query("""
            select r from Role r
            where r.deletedAt is null
              and (:keyword is null or r.roleName like %:keyword% or r.roleCode like %:keyword%)
              and (:roleType is null or r.roleType = :roleType)
            """)
    Page<Role> search(@Param("keyword") String keyword,
                      @Param("roleType") String roleType,
                      Pageable pageable);
}
