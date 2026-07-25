package com.df4j.xctec.xcms.org.repository;

import com.df4j.xctec.xcms.org.domain.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    List<UserGroup> findByDeletedAtIsNull();

    Optional<UserGroup> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByGroupNameAndDeletedAtIsNull(String groupName);
}
