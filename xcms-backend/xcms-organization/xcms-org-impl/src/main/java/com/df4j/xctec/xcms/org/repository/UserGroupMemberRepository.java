package com.df4j.xctec.xcms.org.repository;

import com.df4j.xctec.xcms.org.domain.UserGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGroupMemberRepository extends JpaRepository<UserGroupMember, Long> {

    List<UserGroupMember> findByGroupId(Long groupId);

    Optional<UserGroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    List<UserGroupMember> findByGroupIdAndDeletedAtIsNull(Long groupId);

    long countByGroupId(Long groupId);
}
