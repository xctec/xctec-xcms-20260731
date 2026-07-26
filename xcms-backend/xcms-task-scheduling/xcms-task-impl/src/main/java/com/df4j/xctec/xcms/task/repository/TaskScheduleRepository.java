package com.df4j.xctec.xcms.task.repository;

import com.df4j.xctec.xcms.task.domain.TaskSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskScheduleRepository extends JpaRepository<TaskSchedule, Long>,
        JpaSpecificationExecutor<TaskSchedule> {

    Optional<TaskSchedule> findByTaskCodeAndTenantId(String taskCode, Long tenantId);

    boolean existsByTaskCodeAndTenantId(String taskCode, Long tenantId);

    List<TaskSchedule> findByStatus(String status);
}
