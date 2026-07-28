package com.df4j.xctec.xcms.operation.service;

import com.df4j.xctec.xcms.operation.api.MetricProvider;
import com.df4j.xctec.xcms.operation.api.MetricService;
import com.df4j.xctec.xcms.operation.api.dto.MetricDTO;
import com.df4j.xctec.xcms.operation.api.dto.MetricSample;
import com.df4j.xctec.xcms.operation.api.dto.MetricValueDTO;
import com.df4j.xctec.xcms.operation.domain.Metric;
import com.df4j.xctec.xcms.operation.domain.MetricValue;
import com.df4j.xctec.xcms.operation.repository.MetricRepository;
import com.df4j.xctec.xcms.operation.repository.MetricValueRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService, ApplicationRunner {

    private final MetricRepository metricRepository;
    // AT-26：保留仓储引用以满足过渡期查询；指标不再写入 metric_value，表与查询能力暂留
    private final MetricValueRepository metricValueRepository;
    /** 各业务模块注册的指标采集扩展点 */
    private final ObjectProvider<List<MetricProvider>> metricProviders;
    /** Micrometer 指标注册表（AT-25 引入），供 Prometheus 抓取 */
    private final MeterRegistry meterRegistry;

    /** record()/collect() 动态指标最新值缓存，驱动 Micrometer Gauge（AT-26） */
    private final Map<String, Double> latestValues = new ConcurrentHashMap<>();

    @Override
    public void run(ApplicationArguments args) {
        // 启动即完成各 Provider 的 Meter 注册（AT-25）
        registerProviderMeters();
        // 内置 JVM 指标首注册，确保 Prometheus 始终可抓取
        refreshJvmMeters();
    }

    /** 向 Micrometer 注册所有内置 Provider 的 Meter（AT-25） */
    private void registerProviderMeters() {
        List<MetricProvider> providers = metricProviders.getIfAvailable();
        if (providers == null) {
            return;
        }
        for (MetricProvider provider : providers) {
            try {
                provider.registerMeters(meterRegistry);
            } catch (Exception e) {
                log.warn("[ops] metric provider {} register meters failed", provider.getProviderName(), e);
            }
        }
    }

    @Override
    public void record(String metricKey, BigDecimal value, String tags) {
        // AT-26：废弃 metric_value 落库，改为更新 Micrometer Gauge，由 Prometheus 抓取
        if (!StringUtils.hasText(metricKey) || value == null) {
            return;
        }
        String cacheKey = buildCacheKey(metricKey, tags);
        latestValues.put(cacheKey, value.doubleValue());
        ensureGauge(metricKey, tags, cacheKey);
    }

    @Override
    public void collect() {
        // AT-26：不再落库 metric_value；统一刷新各 Provider 的 Meter 与内置 JVM 指标，由 Prometheus 定时抓取
        refreshJvmMeters();
        List<MetricProvider> providers = metricProviders.getIfAvailable();
        if (providers == null) {
            return;
        }
        for (MetricProvider provider : providers) {
            try {
                provider.registerMeters(meterRegistry);
            } catch (Exception ex) {
                log.warn("[ops] metric provider {} collect failed", provider.getProviderName(), ex);
            }
        }
        log.debug("[ops] metrics meters refreshed");
    }

    /** 刷新内置 JVM 指标（AT-26：以 Gauge 暴露，替代原 metric_value 落库） */
    private void refreshJvmMeters() {
        long usedHeap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        int threads = ManagementFactory.getThreadMXBean().getThreadCount();
        updateGauge("jvm.memory.used.heap", null,
                new BigDecimal(usedHeap).divide(new BigDecimal(1024 * 1024), 2, RoundingMode.HALF_UP));
        updateGauge("jvm.threads.count", null, new BigDecimal(threads));
    }

    private void updateGauge(String metricKey, String tags, BigDecimal value) {
        String cacheKey = buildCacheKey(metricKey, tags);
        latestValues.put(cacheKey, value.doubleValue());
        ensureGauge(metricKey, tags, cacheKey);
    }

    private void ensureGauge(String metricKey, String tags, String cacheKey) {
        Tags micrometerTags = parseTags(tags);
        if (meterRegistry.find(metricKey).tags(micrometerTags).gauge() != null) {
            return;
        }
        Gauge.builder(metricKey, latestValues, map -> {
                    Double v = map.get(cacheKey);
                    return v == null ? 0d : v;
                })
                .tags(micrometerTags)
                .baseUnit("")
                .register(meterRegistry);
    }

    private String buildCacheKey(String metricKey, String tags) {
        return metricKey + "|" + (tags == null ? "" : tags);
    }

    private Tags parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return Tags.empty();
        }
        List<String> kv = new ArrayList<>();
        for (String pair : tags.split(",")) {
            String[] p = pair.split("=", 2);
            if (p.length == 2) {
                kv.add(p[0].trim());
                kv.add(p[1].trim());
            }
        }
        // Tags.of(keyValues...) 接受交替的 key/value 参数
        return Tags.of(kv.toArray(new String[0]));
    }

    @Override
    public List<MetricValueDTO> querySeries(String metricKey, LocalDateTime from, LocalDateTime to) {
        // 过渡期：保留查询能力，历史数据后续由 Prometheus 提供
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
