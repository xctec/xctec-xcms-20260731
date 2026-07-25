package com.df4j.xctec.xcms.auth.repository;

import com.df4j.xctec.xcms.auth.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findByRoleIdIn(List<Long> roleIds);

    Optional<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId);

    void deleteByRoleId(Long roleId);

    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);
}
