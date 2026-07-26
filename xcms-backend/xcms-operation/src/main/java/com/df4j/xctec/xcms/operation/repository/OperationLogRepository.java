package com.df4j.xctec.xcms.operation.repository;

import com.df4j.xctec.xcms.operation.domain.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long>,
        JpaSpecificationExecutor<OperationLog> {

    long countByOccurTimeAfter(java.time.LocalDateTime from);
}
