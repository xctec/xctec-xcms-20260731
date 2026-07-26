package com.df4j.xctec.xcms.operation.repository;

import com.df4j.xctec.xcms.operation.domain.HealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long>,
        JpaSpecificationExecutor<HealthCheck> {

    List<HealthCheck> findByStatus(String status);

    long countByLastStatus(String lastStatus);
}
