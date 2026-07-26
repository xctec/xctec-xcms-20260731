# API Design: Phase 3（Operations / Task Scheduling / SSO）

> 运营管理 + 任务调度 + SSO + 业务可见授权完善 API 接口定义

---

## 1. task-api

### 1.1 TaskScheduleService（定时任务管理面）

```java
package com.df4j.xctec.xcms.task.api;

public interface TaskScheduleService {

    /** 创建定时任务 */
    TaskScheduleDTO createTask(TaskCreateRequest request);

    /** 更新任务 */
    TaskScheduleDTO updateTask(Long taskId, TaskUpdateRequest request);

    /** 启用/停用任务 */
    void toggleTask(Long taskId, boolean enabled);

    /** 手动触发任务 */
    TaskExecutionLogDTO triggerTask(Long taskId);

    /** 删除任务 */
    void deleteTask(Long taskId);

    /** 分页查询任务列表 */
    PageResult<TaskScheduleDTO> listTasks(TaskQuery query);

    /** 获取任务详情 */
    TaskScheduleDTO getTask(Long taskId);

    /** 分页查询执行日志 */
    PageResult<TaskExecutionLogDTO> listExecutionLogs(Long taskId, int page, int size);
}
```

### 1.2 TaskAsyncService（异步任务）

```java
package com.df4j.xctec.xcms.task.api;

public interface TaskAsyncService {

    /** 提交异步任务 */
    Long submitAsync(AsyncTaskRequest request);

    /** 查询异步任务状态 */
    AsyncTaskDTO getAsyncTask(Long asyncTaskId);

    /** 分页查询异步任务 */
    PageResult<AsyncTaskDTO> listAsyncTasks(AsyncTaskQuery query);

    /** 取消未执行的异步任务 */
    void cancelAsyncTask(Long asyncTaskId);
}
```

### 1.3 TaskHandler（任务处理器扩展点）

```java
package com.df4j.xctec.xcms.task.api;

/**
 * 定时任务处理器接口。业务模块实现此接口，在 task_schedule.handler_class 中配置全限定名。
 */
public interface TaskHandler {
    Map<String, Object> execute(Map<String, Object> params);
}
```

### 1.4 DTO

```java
public class TaskCreateRequest {
    private String taskName;
    private String taskCode;           // 租户内唯一
    private String taskType;           // CRON / FIXED_RATE / FIXED_DELAY
    private String cronExpression;
    private Long fixedRate;            // 毫秒
    private Long fixedDelay;           // 毫秒
    private String handlerClass;       // TaskHandler 实现类全限定名
    private Map<String, Object> handlerParams;
    private Integer maxRetry;          // 默认 3
    private Long retryInterval;        // 默认 60000ms
    private String description;
}

public class TaskScheduleDTO {
    private Long id;
    private String taskName;
    private String taskCode;
    private String taskType;
    private String cronExpression;
    private Long fixedRate;
    private Long fixedDelay;
    private String handlerClass;
    private String handlerParams;
    private String status;             // ENABLED / DISABLED
    private Integer maxRetry;
    private Long retryInterval;
    private LocalDateTime lastExecAt;
    private LocalDateTime nextExecAt;
    private String description;
}

public class TaskExecutionLogDTO {
    private Long id;
    private Long taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;             // SUCCESS / FAILED / RUNNING / TIMEOUT
    private String result;
    private String errorMsg;
    private Integer retryCount;
}

public class AsyncTaskRequest {
    private String taskType;
    private Map<String, Object> payload;
    private Integer priority;          // 0-9，默认 5
    private LocalDateTime scheduledAt; // null=立即执行
}

public class AsyncTaskDTO {
    private Long id;
    private String taskType;
    private String payload;
    private String status;             // PENDING / RUNNING / SUCCESS / FAILED
    private Integer priority;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer retryCount;
    private String errorMsg;
}
```

---

## 2. operation-api

### 2.1 DashboardService（运营看板）

```java
package com.df4j.xctec.xcms.operation.api;

public interface DashboardService {

    /** 获取看板概览（聚合各分类最新指标） */
    DashboardOverviewDTO getOverview();

    /** 获取指定分类的指标历史趋势 */
    List<MetricTrendDTO> getMetricTrend(String category, String metricKey, int days);

    /** 获取资源使用 TOP N */
    List<TenantUsageDTO> getTopUsage(String metricKey, int limit);

    /** 看板配置 CRUD */
    DashboardConfigDTO saveDashboardConfig(DashboardConfigRequest request);
    List<DashboardConfigDTO> listDashboardConfigs();
    void deleteDashboardConfig(Long configId);
}
```

### 2.2 AlertService（监控告警）

```java
package com.df4j.xctec.xcms.operation.api;

public interface AlertService {

    PageResult<AlertRuleDTO> listAlertRules(AlertRuleQuery query);
    AlertRuleDTO createAlertRule(AlertRuleCreateRequest request);
    AlertRuleDTO updateAlertRule(Long ruleId, AlertRuleUpdateRequest request);
    void deleteAlertRule(Long ruleId);

    PageResult<AlertRecordDTO> listAlertRecords(AlertRecordQuery query);
    void acknowledgeAlert(Long alertId);
    void resolveAlert(Long alertId);
}
```

### 2.3 MeteringService（计量管理）

```java
package com.df4j.xctec.xcms.operation.api;

public interface MeteringService {

    MeteringOverviewDTO getOverview(String period);
    PageResult<MeteringRecordDTO> listRecords(MeteringQuery query);

    MeteringRuleDTO createRule(MeteringRuleRequest request);
    MeteringRuleDTO updateRule(Long ruleId, MeteringRuleRequest request);
    void deleteRule(Long ruleId);
    List<MeteringRuleDTO> listRules();

    byte[] exportReport(MeteringQuery query);
}
```

### 2.4 MetricProvider（指标提供者扩展点）

```java
package com.df4j.xctec.xcms.operation.api;

/**
 * 指标提供者接口。各模块实现此接口，由 MetricSnapshotHandler 定时调用采集。
 */
public interface MetricProvider {
    String getMetricCategory();                    // TENANT/USER/STORAGE/WORKFLOW/MESSAGE/AUDIT
    List<MetricData> collect(Long tenantId);
}

public class MetricData {
    private String metricKey;
    private Long metricValue;
    private String metricUnit;
}
```

### 2.5 DTO

```java
public class DashboardOverviewDTO {
    private List<MetricSnapshotDTO> tenantMetrics;
    private List<MetricSnapshotDTO> userMetrics;
    private List<MetricSnapshotDTO> storageMetrics;
    private List<MetricSnapshotDTO> workflowMetrics;
    private List<MetricSnapshotDTO> messageMetrics;
    private List<MetricSnapshotDTO> auditMetrics;
    private List<AlertRecordDTO> activeAlerts;
}

public class MetricSnapshotDTO {
    private String metricCategory;
    private String metricKey;
    private Long metricValue;
    private String metricUnit;
    private LocalDateTime snapshotTime;
}

public class MetricTrendDTO {
    private LocalDateTime snapshotTime;
    private Long metricValue;
}

public class TenantUsageDTO {
    private Long tenantId;
    private String tenantName;
    private Long metricValue;
    private String metricUnit;
    private Double usagePercent;
}

public class AlertRuleCreateRequest {
    private String ruleName;
    private String metricKey;
    private String operator;          // GT/LT/GTE/LTE/EQ
    private BigDecimal threshold;
    private Integer durationSec;
    private String alertLevel;        // INFO/WARN/CRITICAL
    private List<String> notifyChannels;
    private List<Long> notifyRoleIds;
    private Boolean enabled;
}

public class AlertRecordDTO {
    private Long id;
    private Long ruleId;
    private String alertLevel;
    private String alertTitle;
    private String alertContent;
    private BigDecimal metricValue;
    private String status;            // ACTIVE/ACKNOWLEDGED/RESOLVED
    private Long acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}

public class MeteringOverviewDTO {
    private String period;
    private List<MeteringRecordDTO> records;
    private BigDecimal totalCost;
}

public class MeteringRecordDTO {
    private Long id;
    private String meteringType;
    private Long meteringValue;
    private String period;
    private BigDecimal cost;
}
```

---

## 3. sso-api

### 3.1 SsoService（SSO 登录）

```java
package com.df4j.xctec.xcms.identity.api.sso;

public interface SsoService {

    /**
     * 发起 SSO 登录，返回 SSO 授权重定向 URL
     */
    SsoAuthorizeDTO initiate(Long configId);

    /**
     * SSO 回调处理：用授权码换取 Token，获取用户信息，映射本地用户，创建会话
     */
    SsoLoginResultDTO callback(SsoCallbackRequest request);

    /**
     * 获取可用的 SSO 配置列表（供登录页展示登录入口）
     */
    List<SsoConfigDTO> listEnabledConfigs();
}
```

### 3.2 SsoConfigService（SSO 配置管理面）

```java
package com.df4j.xctec.xcms.identity.api.sso;

public interface SsoConfigService {

    SsoConfigDTO createConfig(SsoConfigCreateRequest request);
    SsoConfigDTO updateConfig(Long configId, SsoConfigUpdateRequest request);
    void deleteConfig(Long configId);
    void toggleConfig(Long configId, boolean enabled);
    PageResult<SsoConfigDTO> listConfigs(SsoConfigQuery query);
    SsoConfigDTO getConfig(Long configId);

    /** 测试 SSO 连通性（验证 client_id/secret/端点是否可用） */
    SsoTestResultDTO testConnection(Long configId);
}
```

### 3.3 DTO

```java
public class SsoAuthorizeDTO {
    private String authorizeUrl;      // 重定向到 SSO 的完整 URL
    private String state;             // CSRF 防护 token
}

public class SsoCallbackRequest {
    private String code;              // SSO 授权码
    private String state;             // 与 initiate 返回的 state 校验
    private Long configId;            // SSO 配置 ID
}

public class SsoLoginResultDTO {
    private boolean success;
    private String token;             // XCMS 会话 Token（success=true 时）
    private String redirectUrl;       // 登录成功后的跳转地址
    private String errorMsg;          // 失败原因
    private Long localUserId;         // 映射的本地用户 ID
}

public class SsoConfigDTO {
    private Long id;
    private String configName;
    private String protocol;          // OAUTH2 / OIDC
    private String clientId;
    private String authorizeUrl;
    private String tokenUrl;
    private String userinfoUrl;
    private String redirectUrl;
    private String scopes;
    private Boolean enabled;
    private Boolean autoCreateUser;
    private String userMappingField;  // EMAIL / USERNAME / EMPLOYEE_NO
    private Long defaultRoleId;
}

public class SsoConfigCreateRequest {
    private String configName;
    private String protocol;
    private String clientId;
    private String clientSecret;      // 明文传入，服务端加密存储
    private String issuerUrl;
    private String authorizeUrl;
    private String tokenUrl;
    private String userinfoUrl;
    private String redirectUrl;
    private String scopes;
    private Boolean autoCreateUser;
    private String userMappingField;
    private Long defaultRoleId;
    private String config;            // 扩展配置 JSON
}

public class SsoTestResultDTO {
    private boolean tokenEndpointOk;
    private boolean userinfoEndpointOk;
    private String testUserId;
    private String errorMsg;
}
```

---

## 4. 跨租户业务可见授权完善

> Phase 2 已实现 `CrossTenantAuth` 核心实体和 `BusinessVisibilityAuthService`，Phase 3 补充令牌查询管理接口。

### 4.1 补充接口

```java
package com.df4j.xctec.xcms.auth.api;

public interface CrossTenantAuthService {

    /** 已有：发起授权申请 */
    CrossTenantAuthDTO applyAuthorization(CrossTenantAuthRequest request);

    /** 已有：审批通过 */
    CrossTenantAuthDTO approveAuthorization(Long authId, String approvedBy);

    /** 已有：拒绝 */
    void rejectAuthorization(Long authId, String rejectedBy, String reason);

    /** 已有：撤销 */
    void revokeAuthorization(Long authId, String revokedBy, String reason);

    /** Phase 3 新增：分页查询授权列表（管理面） */
    PageResult<CrossTenantAuthDTO> listAuthorizations(CrossTenantAuthQuery query);

    /** Phase 3 新增：获取当前用户的有效授权（业务面，供前端展示已授权的目标租户） */
    List<CrossTenantAuthDTO> getMyActiveAuthorizations(Long userId);

    /** Phase 3 新增：主动激活授权（PENDING → APPROVED → ACTIVE 两步审批场景） */
    CrossTenantAuthDTO activateAuthorization(Long authId, String activatedBy);
}
```

### 4.2 补充 DTO

```java
public class CrossTenantAuthQuery {
    private Long sourceTenantId;       // 发起方租户
    private Long targetTenantId;       // 目标租户
    private Long userId;               // 被授权用户
    private String status;             // PENDING/APPROVED/ACTIVE/EXPIRED/REVOKED/REJECTED
    private int page = 1;
    private int size = 20;
}

public class CrossTenantAuthDTO {
    private Long id;
    private Long sourceTenantId;
    private String sourceTenantName;
    private Long targetTenantId;
    private String targetTenantName;
    private Long userId;
    private String userName;
    private String bizModule;          // 授权的业务模块
    private String dataScope;          // 数据范围（JSON）
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String status;
    private String reason;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
```

---

## 5. 模块依赖关系

```
                    ┌─────────────┐
                    │ task-scheduling │ ← 底层基础设施
                    └──────┬──────┘
                           │ 被依赖
            ┌──────────────┼──────────────┐
            ▼              ▼              ▼
     ┌─────────────┐ ┌───────────┐ ┌───────────┐
     │  operation  │ │  audit    │ │ workflow  │
     │ (指标采集)  │ │ (日志清理)│ │ (超时处理)│
     └──────┬──────┘ └───────────┘ └───────────┘
            │ 依赖各模块数据
            ▼
     ┌─────────────┐
     │  operation  │ ← 聚合看板/告警/计量
     └─────────────┘

     ┌─────────────┐
     │     sso     │ ← 独立增强 identity，可并行开发
     └─────────────┘
```

| 模块 | 依赖 | 被依赖 |
|------|------|--------|
| task-scheduling | shared-kernel, configuration | operation, audit, message, workflow |
| operation | task-scheduling, 各业务模块（MetricProvider） | - |
| sso | identity | - |
| 跨租户授权完善 | authorization（已有）| - |

---
