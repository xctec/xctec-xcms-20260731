package com.df4j.xctec.xcms.operation.service;

import com.df4j.xctec.xcms.operation.api.DashboardService;
import com.df4j.xctec.xcms.operation.api.MetricService;
import com.df4j.xctec.xcms.operation.api.dto.DashboardDTO;
import com.df4j.xctec.xcms.operation.repository.AlertRuleRepository;
import com.df4j.xctec.xcms.operation.repository.HealthCheckRepository;
import com.df4j.xctec.xcms.operation.repository.MetricRepository;
import com.df4j.xctec.xcms.operation.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final HealthCheckRepository healthCheckRepository;
    private final MetricRepository metricRepository;
    private final OperationLogRepository operationLogRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final MetricService metricService;

    @Override
    public DashboardDTO getDashboard() {
        DashboardDTO dto = new DashboardDTO();
        dto.setHealthTotal(healthCheckRepository.count());
        dto.setHealthUp(healthCheckRepository.countByLastStatus("UP"));
        dto.setHealthDown(healthCheckRepository.countByLastStatus("DOWN"));
        dto.setMetricsTotal(metricRepository.count());
        dto.setOperationLogsToday(operationLogRepository.countByOccurTimeAfter(
                LocalDateTime.now().toLocalDate().atStartOfDay()));
        dto.setAlertRulesEnabled(alertRuleRepository.findByEnabledTrue().size());

        Map<String, String> latest = new LinkedHashMap<>();
        for (String key : new String[]{"jvm.memory.used.heap", "jvm.threads.count"}) {
            var v = metricService.getLatest(key);
            latest.put(key, v != null && v.getValue() != null ? v.getValue().toString() : "-");
        }
        dto.setLatestMetrics(latest);
        return dto;
    }
}
