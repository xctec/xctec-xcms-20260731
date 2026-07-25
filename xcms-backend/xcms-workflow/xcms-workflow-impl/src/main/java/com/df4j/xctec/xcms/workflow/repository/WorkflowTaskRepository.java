package com.df4j.xctec.xcms.workflow.repository;

import com.df4j.xctec.xcms.workflow.domain.WorkflowTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {
    Optional<WorkflowTask> findByFlowTaskId(String flowTaskId);

    Page<WorkflowTask> findByAssigneeId(Long assigneeId, Pageable pageable);

    List<WorkflowTask> findByInstanceId(Long instanceId);
}
