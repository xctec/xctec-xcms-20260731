package com.df4j.xctec.xcms.task.repository;

import com.df4j.xctec.xcms.task.domain.TaskExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskExecutionLogRepository extends JpaRepository<TaskExecutionLog, Long>,
        JpaSpecificationExecutor<TaskExecutionLog> {

    Page<TaskExecutionLog> findByTaskIdOrderByStartTimeDesc(Long taskId, Pageable pageable);
}
