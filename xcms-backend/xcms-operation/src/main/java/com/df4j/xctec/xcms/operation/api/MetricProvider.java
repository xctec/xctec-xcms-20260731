package com.df4j.xctec.xcms.operation.api;

import com.df4j.xctec.xcms.operation.api.dto.MetricSample;

import java.util.List;

/**
 * 指标采集扩展点（SPI）。
 * <p>
 * 各业务模块（identity/workflow/message 等）可实现该接口，由运维中心的 {@code MetricSnapshotHandler}
 * 定时触发 {@link MetricService#collect()} 时统一遍历采集，从而实现跨模块指标自动上报，
 * 无需在运维模块反向依赖各业务模块。
 */
public interface MetricProvider {

    /**
     * 采集来源名称，便于排查与分组（如 "identity" / "workflow"）。
     */
    String getProviderName();

    /**
     * 采集一批指标样本。实现方应保证快速返回、无副作用。
     */
    List<MetricSample> collectMetrics();
}
