package com.df4j.xctec.xcms.operation.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.operation.api.AlertRuleService;
import com.df4j.xctec.xcms.operation.api.DashboardService;
import com.df4j.xctec.xcms.operation.api.HealthCheckService;
import com.df4j.xctec.xcms.operation.api.MetricService;
import com.df4j.xctec.xcms.operation.api.OperationLogService;
import com.df4j.xctec.xcms.operation.api.dto.AlertRecordDTO;
import com.df4j.xctec.xcms.operation.api.dto.AlertRuleDTO;
import com.df4j.xctec.xcms.operation.api.dto.DashboardDTO;
import com.df4j.xctec.xcms.operation.api.dto.HealthCheckDTO;
import com.df4j.xctec.xcms.operation.api.dto.HealthCheckLogDTO;
import com.df4j.xctec.xcms.operation.api.dto.MetricDTO;
import com.df4j.xctec.xcms.operation.api.dto.MetricValueDTO;
import com.df4j.xctec.xcms.operation.api.dto.OperationLogDTO;
import com.df4j.xctec.xcms.operation.api.dto.request.AlertRuleCreateRequest;
import com.df4j.xctec.xcms.operation.api.dto.request.AlertRuleQuery;
import com.df4j.xctec.xcms.operation.api.dto.request.AlertRuleUpdateRequest;
import com.df4j.xctec.xcms.operation.api.dto.request.HealthCheckCreateRequest;
import com.df4j.xctec.xcms.operation.api.dto.request.HealthCheckQuery;
import com.df4j.xctec.xcms.operation.api.dto.request.HealthCheckUpdateRequest;
import com.df4j.xctec.xcms.operation.api.dto.request.MetricQuery;
import com.df4j.xctec.xcms.operation.api.dto.request.MetricRecordRequest;
import com.df4j.xctec.xcms.operation.api.dto.request.OperationLogQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/ops")
@RequiredArgsConstructor
public class OperationController {

    private final MetricService metricService;
    private final HealthCheckService healthCheckService;
    private final OperationLogService operationLogService;
    private final DashboardService dashboardService;
    private final AlertRuleService alertRuleService;

    @PostMapping("/metric/definitions")
    public ApiResponse<List<MetricDTO>> metricDefinitions() {
        return ApiResponse.success(metricService.listDefinitions());
    }

    @PostMapping("/metric/series")
    public ApiResponse<List<MetricValueDTO>> metricSeries(@RequestBody MetricQuery query) {
        return ApiResponse.success(metricService.querySeries(query.getMetricKey(), query.getFrom(), query.getTo()));
    }

    @PostMapping("/metric/record")
    public ApiResponse<Void> metricRecord(@RequestBody MetricRecordRequest request) {
        metricService.record(request.getMetricKey(), request.getValue(), request.getTags());
        return ApiResponse.success();
    }

    @PostMapping("/health/create")
    public ApiResponse<HealthCheckDTO> healthCreate(@RequestBody HealthCheckCreateRequest request) {
        return ApiResponse.success(healthCheckService.create(request));
    }

    @PostMapping("/health/update")
    public ApiResponse<HealthCheckDTO> healthUpdate(@RequestBody HealthCheckUpdateRequest request) {
        return ApiResponse.success(healthCheckService.update(request));
    }

    @PostMapping("/health/delete")
    public ApiResponse<Void> healthDelete(@RequestBody IdRequest request) {
        healthCheckService.delete(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/health/enable")
    public ApiResponse<Void> healthEnable(@RequestBody IdRequest request) {
        healthCheckService.enable(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/health/disable")
    public ApiResponse<Void> healthDisable(@RequestBody IdRequest request) {
        healthCheckService.disable(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/health/run")
    public ApiResponse<Void> healthRun(@RequestBody IdRequest request) {
        healthCheckService.run(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/health/get")
    public ApiResponse<HealthCheckDTO> healthGet(@RequestBody IdRequest request) {
        return ApiResponse.success(healthCheckService.get(request.getId()));
    }

    @PostMapping("/health/list")
    public ApiResponse<PageResult<HealthCheckDTO>> healthList(@RequestBody HealthCheckQuery query) {
        return ApiResponse.success(healthCheckService.list(query));
    }

    @PostMapping("/health/logs")
    public ApiResponse<PageResult<HealthCheckLogDTO>> healthLogs(@RequestBody IdRequest request) {
        return ApiResponse.success(healthCheckService.logs(request.getId(), 1, 20));
    }

    @PostMapping("/log/list")
    public ApiResponse<PageResult<OperationLogDTO>> logList(@RequestBody OperationLogQuery query) {
        return ApiResponse.success(operationLogService.list(query));
    }

    @PostMapping("/log/get")
    public ApiResponse<OperationLogDTO> logGet(@RequestBody IdRequest request) {
        return ApiResponse.success(operationLogService.get(request.getId()));
    }

    @PostMapping("/dashboard")
    public ApiResponse<DashboardDTO> dashboard() {
        return ApiResponse.success(dashboardService.getDashboard());
    }

    @PostMapping("/alert/rule/create")
    public ApiResponse<AlertRuleDTO> alertCreate(@RequestBody AlertRuleCreateRequest request) {
        return ApiResponse.success(alertRuleService.create(request));
    }

    @PostMapping("/alert/rule/update")
    public ApiResponse<AlertRuleDTO> alertUpdate(@RequestBody AlertRuleUpdateRequest request) {
        return ApiResponse.success(alertRuleService.update(request));
    }

    @PostMapping("/alert/rule/delete")
    public ApiResponse<Void> alertDelete(@RequestBody IdRequest request) {
        alertRuleService.delete(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/alert/rule/enable")
    public ApiResponse<Void> alertEnable(@RequestBody IdRequest request) {
        alertRuleService.enable(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/alert/rule/disable")
    public ApiResponse<Void> alertDisable(@RequestBody IdRequest request) {
        alertRuleService.disable(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/alert/rule/get")
    public ApiResponse<AlertRuleDTO> alertGet(@RequestBody IdRequest request) {
        return ApiResponse.success(alertRuleService.get(request.getId()));
    }

    @PostMapping("/alert/rule/list")
    public ApiResponse<PageResult<AlertRuleDTO>> alertList(@RequestBody AlertRuleQuery query) {
        return ApiResponse.success(alertRuleService.list(query));
    }

    @PostMapping("/alert/record/list")
    public ApiResponse<PageResult<AlertRecordDTO>> alertRecords() {
        return ApiResponse.success(alertRuleService.records(1, 20));
    }
}
