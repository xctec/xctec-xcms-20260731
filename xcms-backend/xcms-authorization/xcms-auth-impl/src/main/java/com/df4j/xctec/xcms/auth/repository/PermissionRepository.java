package com.df4j.xctec.xcms.auth.repository;

import com.df4j.xctec.xcms.auth.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermCode(String permCode);

    List<Permission> findByIdIn(List<Long> ids);
}
