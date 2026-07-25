package com.df4j.xctec.xcms.org.repository;

import com.df4j.xctec.xcms.org.domain.UserPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPositionRepository extends JpaRepository<UserPosition, Long> {

    List<UserPosition> findByUserId(Long userId);

    List<UserPosition> findByDeptId(Long deptId);

    Optional<UserPosition> findByUserIdAndPositionId(Long userId, Long positionId);

    void deleteByUserIdAndPositionId(Long userId, Long positionId);

    Page<UserPosition> findByDeptIdAndDeletedAtIsNull(Long deptId, Pageable pageable);

    List<UserPosition> findByUserIdAndIsPrimaryTrue(Long userId);
}
