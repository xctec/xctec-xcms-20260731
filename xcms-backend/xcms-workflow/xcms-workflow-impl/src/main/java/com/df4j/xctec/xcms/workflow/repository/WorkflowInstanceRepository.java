package com.df4j.xctec.xcms.workflow.repository;

import com.df4j.xctec.xcms.workflow.domain.WorkflowInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    Page<WorkflowInstance> findByStatus(String status, Pageable pageable);

    List<WorkflowInstance> findByInitiatorId(Long initiatorId);
}
