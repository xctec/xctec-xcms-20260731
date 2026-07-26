package com.df4j.xctec.xcms.workflow.repository;

import com.df4j.xctec.xcms.workflow.domain.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    Optional<WorkflowDefinition> findByProcDefKeyAndVersion(String procDefKey, int version);
}
