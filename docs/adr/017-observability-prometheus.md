# ADR-017: 可观测性采用 Prometheus + Grafana + Alertmanager

- **Status**: Accepted
- **Date**: 2026-07-27

## Context

当前指标采集是 push 式自建设施：`MetricServiceImpl.collect()` 从 `ManagementFactory` 取 JVM 指标 + 遍历 `MetricProvider` SPI，每条样本 `save` 进关系库 `metric_value`。告警是自建玩具引擎：`AlertRuleServiceImpl.evaluate()` 仅取 `getLatest` 瞬时值比对，规则字段 `durationMin` **完全未使用**（无"持续 N 分钟"语义），无 PromQL/多条件/抑制/静默/路由（复盘 S15）。

问题：时序数据存关系库高基数写 OLTP、无实例维度、多实例错乱；JVM 指标手写落后；告警引擎玩具级含真实缺陷；前端运营看板静态 mock 缺计量（F6）。

## Decision

迁移业界标准可观测栈，废弃自建指标落库与告警引擎：

1. **采集**：加 `micrometer-registry-prometheus`，暴露 `/actuator/prometheus`。`MetricProvider` SPI 语义从"落 DB"改为"向 Micrometer 注册 `Meter`（Counter/Gauge/Timer）"，业务指标自动进 Prometheus。JVM/缓存指标由 actuator 自动暴露。
2. **存储/可视化**：Prometheus 拉取 + Grafana 仪表盘。多租户以 `tenant` label 过滤（租户基数大时仅关键租户打 label，或远程写分片，避免 TSDB 基数爆炸）。
3. **告警**：PromQL + `for: 5m` 持续时间 + 分组/抑制/静默 + 多通道（邮件/钉钉/Webhook），用 Alertmanager。`AlertRule` 表改为消费 Alertmanager webhook 归档 `alert_record`，不自算阈值——`durationMin` 终于生效。
4. **废弃**：移除 `metric_value` 落库；`MetricServiceImpl.collect()` 改为注册 Meter 或作自定义聚合兜底。
5. **解决**：F6 运营看板静态 mock → Grafana 嵌入/数据源替代。

## Alternatives

### 方案 B：保留自建指标落库 + 完善告警引擎

- **优点**：不引入外部组件
- **缺点**：时序存关系库性能差、无实例维度、告警引擎需重写支持持续语义/抑制/路由，重复造轮子

### 方案 C：InfluxDB + 自建看板

- **优点**：时序库专业
- **缺点**：告警/可视化仍需自建，生态不如 Prometheus

## Consequences

### 正面

- 业界标准栈，生态成熟，告警/可视化开箱即用
- 指标 pull 模式天然按 instance 打标，与无状态多实例（ADR-012）契合
- `durationMin` 等持续语义生效，告警能力达生产级
- 解决 F6 看板

### 负面

- 引入 Prometheus/Grafana/Alertmanager 运维组件
- 多租户 label 需控制基数
- `MetricProvider` SPI 语义改造，`metric_value` 迁移

### 缓解措施

- 单体阶段可先用 actuator + Micrometer 暴露，Prometheus/Grafana 按需部署
- 多租户 label 谨慎，仅关键租户打标
- `MetricProvider` SPI 保留作"业务指标注册点"（良好扩展设计），`collect()` 改为注册 Meter
