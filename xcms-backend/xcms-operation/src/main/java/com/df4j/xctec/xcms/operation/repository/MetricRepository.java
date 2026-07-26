package com.df4j.xctec.xcms.operation.repository;

import com.df4j.xctec.xcms.operation.domain.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {
    Optional<Metric> findByMetricKey(String metricKey);
}
