package com.df4j.xctec.xcms.operation.repository;

import com.df4j.xctec.xcms.operation.domain.HealthCheckLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthCheckLogRepository extends JpaRepository<HealthCheckLog, Long>,
        JpaSpecificationExecutor<HealthCheckLog> {

    Page<HealthCheckLog> findByCheckIdOrderByCheckTimeDesc(Long checkId, Pageable pageable);
}
