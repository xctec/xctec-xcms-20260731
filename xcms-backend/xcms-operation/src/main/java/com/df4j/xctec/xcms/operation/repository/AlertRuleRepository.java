package com.df4j.xctec.xcms.operation.repository;

import com.df4j.xctec.xcms.operation.domain.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long>,
        JpaSpecificationExecutor<AlertRule> {

    List<AlertRule> findByEnabledTrue();

    Optional<AlertRule> findByMetricKeyAndEnabledTrue(String metricKey);
}
