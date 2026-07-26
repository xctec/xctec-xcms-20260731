package com.df4j.xctec.xcms.operation.repository;

import com.df4j.xctec.xcms.operation.domain.MetricValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetricValueRepository extends JpaRepository<MetricValue, Long>,
        JpaSpecificationExecutor<MetricValue> {

    List<MetricValue> findByMetricKeyAndCollectTimeBetweenOrderByCollectTimeAsc(
            String metricKey, LocalDateTime from, LocalDateTime to);

    Optional<MetricValue> findTopByMetricKeyOrderByCollectTimeDesc(String metricKey);
}
