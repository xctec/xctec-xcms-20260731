package com.df4j.xctec.xcms.operation.api;

import com.df4j.xctec.xcms.operation.api.dto.MetricDTO;
import com.df4j.xctec.xcms.operation.api.dto.MetricValueDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 指标采集服务。
 */
public interface MetricService {

    /** 记录一条指标值 */
    void record(String metricKey, BigDecimal value, String tags);

    /** 采集内置指标（由定时任务 MetricSnapshotHandler 调用） */
    void collect();

    /** 查询指标时序 */
    List<MetricValueDTO> querySeries(String metricKey, LocalDateTime from, LocalDateTime to);

    /** 最新指标值 */
    MetricValueDTO getLatest(String metricKey);

    /** 指标定义列表 */
    List<MetricDTO> listDefinitions();
}
