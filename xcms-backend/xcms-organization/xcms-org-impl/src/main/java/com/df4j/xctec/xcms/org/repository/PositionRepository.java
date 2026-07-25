package com.df4j.xctec.xcms.org.repository;

import com.df4j.xctec.xcms.org.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByDeptIdAndDeletedAtIsNull(Long deptId);

    Optional<Position> findByIdAndDeletedAtIsNull(Long id);
}
