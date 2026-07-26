package com.df4j.xctec.xcms.operation.handler;

import com.df4j.xctec.xcms.operation.api.MetricService;
import com.df4j.xctec.xcms.task.api.TaskHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 内置任务处理器：采集指标快照。由定时任务（handler_name=METRIC_SNAPSHOT）周期触发。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricSnapshotHandler implements TaskHandler {

    private final MetricService metricService;

    @Override
    public String getHandlerName() {
        return "METRIC_SNAPSHOT";
    }

    @Override
    public String getGroup() {
        return "metrics";
    }

    @Override
    public void execute(Map<String, Object> params) {
        metricService.collect();
    }
}
