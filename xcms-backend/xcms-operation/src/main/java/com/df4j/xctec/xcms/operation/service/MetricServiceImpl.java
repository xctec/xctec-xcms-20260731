package com.df4j.xctec.xcms.operation.service;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.operation.api.MetricProvider;
import com.df4j.xctec.xcms.operation.api.MetricService;
import com.df4j.xctec.xcms.operation.api.dto.MetricDTO;
import com.df4j.xctec.xcms.operation.api.dto.MetricSample;
import com.df4j.xctec.xcms.operation.api.dto.MetricValueDTO;
import com.df4j.xctec.xcms.operation.domain.Metric;
import com.df4j.xctec.xcms.operation.domain.MetricValue;
import com.df4j.xctec.xcms.operation.repository.MetricRepository;
import com.df4j.xctec.xcms.operation.repository.MetricValueRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService, ApplicationRunner {

    private final MetricRepository metricRepository;
    private final MetricValueRepository metricValueRepository;
    /** 各业务模块注册的指标采集扩展点 */
    private final ObjectProvider<MetricProvider> metricProviders;
    /** AT-25：Micrometer 指标注册表，供 Prometheus 抓取 */
    private final MeterRegistry meterRegistry;

    @Override
    public void run(ApplicationArguments args) {
        // AT-25：应用启动即完成各 Provider 的 Meter 注册，供 Prometheus 抓取
        registerProviderMeters();
    }

    /** 向 Micrometer 注册所有内置 Provider 的 Meter（AT-25） */
    private void registerProviderMeters() {
        for (MetricProvider provider : metricProviders) {
            try {
                provider.registerMeters(meterRegistry);
            } catch (Exception e) {
                log.warn("[ops] metric provider {} register meters failed", provider.getProviderName(), e);
            }
        }
    }

    @Override
    @Transactional
    public void record(String metricKey, BigDecimal value, String tags) {
        Long tenantId = TenantContext.getTenantId();
        MetricValue v = new MetricValue();
        v.setTenantId(tenantId);
        v.setMetricKey(metricKey);
        v.setMetricName(metricRepository.findByMetricKey(metricKey).map(Metric::getMetricName).orElse(metricKey));
        v.setValue(value);
        v.setTags(tags);
        v.setSource("manual");
        v.setCollectTime(LocalDateTime.now());
        metricValueRepository.save(v);
    }

    @Override
    @Transactional
    public void collect() {
        long usedHeap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        int threads = ManagementFactory.getThreadMXBean().getThreadCount();
        Long tenantId = TenantContext.getTenantId();
        recordInternal(tenantId, "jvm.memory.used.heap", new BigDecimal(usedHeap).divide(new BigDecimal(1024 * 1024), 2, RoundingMode.HALF_UP), "MB", "system");
        recordInternal(tenantId, "jvm.threads.count", new BigDecimal(threads), "", "system");

        // 遍历各业务模块注册的 MetricProvider，统一采集跨模块指标
        for (MetricProvider provider : metricProviders) {
            try {
                List<MetricSample> samples = provider.collectMetrics();
                if (samples == null) {
                    continue;
                }
                for (MetricSample s : samples) {
                    if (s == null || s.getMetricKey() == null || s.getValue() == null) {
                        continue;
                    }
                    ensureDefinition(tenantId, s.getMetricKey(),
                            s.getUnit() != null ? s.getUnit() : "",
                            s.getCategory() != null ? s.getCategory() : "business");
                    MetricValue v = new MetricValue();
                    v.setTenantId(tenantId);
                    v.setMetricKey(s.getMetricKey());
                    v.setMetricName(s.getMetricName() != null ? s.getMetricName() : s.getMetricKey());
                    v.setValue(s.getValue());
                    v.setTags(s.getTags());
                    v.setSource(provider.getProviderName());
                    v.setCollectTime(LocalDateTime.now());
                    metricValueRepository.save(v);
                }
            } catch (Exception ex) {
                log.warn("[ops] metric provider collect failed: {}", provider.getProviderName(), ex);
            }
        }
        log.debug("[ops] metrics collected");
    }

    private void recordInternal(Long tenantId, String key, BigDecimal value, String unit, String category) {
        ensureDefinition(tenantId, key, unit, category);
        MetricValue v = new MetricValue();
        v.setTenantId(tenantId);
        v.setMetricKey(key);
        v.setMetricName(key);
        v.setValue(value);
        v.setSource("collector");
        v.setCollectTime(LocalDateTime.now());
        metricValueRepository.save(v);
    }

    private void ensureDefinition(Long tenantId, String key, String unit, String category) {
        if (metricRepository.findByMetricKey(key).isEmpty()) {
            Metric m = new Metric();
            m.setTenantId(tenantId);
            m.setMetricKey(key);
            m.setMetricName(key);
            m.setCategory(category);
            m.setUnit(unit);
            metricRepository.save(m);
        }
    }

    @Override
    public List<MetricValueDTO> querySeries(String metricKey, LocalDateTime from, LocalDateTime to) {
        LocalDateTime f = from != null ? from : LocalDateTime.now().minusDays(1);
        LocalDateTime t = to != null ? to : LocalDateTime.now();
        return metricValueRepository.findByMetricKeyAndCollectTimeBetweenOrderByCollectTimeAsc(metricKey, f, t)
                .stream().map(this::toValueDto).toList();
    }

    @Override
    public MetricValueDTO getLatest(String metricKey) {
        Optional<MetricValue> v = metricValueRepository.findTopByMetricKeyOrderByCollectTimeDesc(metricKey);
        return v.map(this::toValueDto).orElse(null);
    }

    @Override
    public List<MetricDTO> listDefinitions() {
        return metricRepository.findAll().stream().map(this::toDto).toList();
    }

    private MetricValueDTO toValueDto(MetricValue v) {
        MetricValueDTO dto = new MetricValueDTO();
        dto.setId(v.getId());
        dto.setTenantId(v.getTenantId());
        dto.setMetricKey(v.getMetricKey());
        dto.setMetricName(v.getMetricName());
        dto.setValue(v.getValue());
        dto.setTags(v.getTags());
        dto.setSource(v.getSource());
        dto.setCollectTime(v.getCollectTime());
        return dto;
    }

    private MetricDTO toDto(Metric m) {
        MetricDTO dto = new MetricDTO();
        dto.setId(m.getId());
        dto.setTenantId(m.getTenantId());
        dto.setMetricKey(m.getMetricKey());
        dto.setMetricName(m.getMetricName());
        dto.setCategory(m.getCategory());
        dto.setUnit(m.getUnit());
        dto.setDescription(m.getDescription());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        return dto;
    }
}
