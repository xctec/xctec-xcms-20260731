package com.df4j.xctec.xcms.identity.repository;

import com.df4j.xctec.xcms.identity.domain.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    Page<UserRole> findByRoleId(Long roleId, Pageable pageable);

    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    /** 显式带 tenantId 的查询：租户初始化等跨租户场景使用，不依赖隐式 @TenantId 过滤 */
    Optional<UserRole> findByTenantIdAndUserIdAndRoleId(Long tenantId, Long userId, Long roleId);

    Optional<UserRole> findByUserIdAndRoleIdAndScopeTypeAndScopeValue(Long userId, Long roleId, String scopeType, String scopeValue);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);

    void deleteByRoleId(Long roleId);
}
