# ADR-005: Flowable 共享引擎

- **Status**: Accepted
- **Date**: 2026-07-24

## Context

XCMS 流程中心需要 BPM 引擎，且需要支持跨租户流程。需要决定：
- 流程引擎是共享一个实例还是每租户独立？
- 流程数据是集中存储还是分散存储？

## Decision

采用 **Flowable 共享引擎**：一个 Flowable 实例内嵌在单体应用中，服务所有租户。所有租户的流程数据存储在同一个数据库，通过 Flowable 原生的 `TENANT_ID_` 列隔离。

使用 **Discriminator 多租户模式**，与 JPA `@TenantId` 思路一致。

## Alternatives

### 方案 B：每租户独立引擎

- **优点**：物理隔离
- **缺点**：跨租户流程极难实现（需跨引擎调用）、资源成本高（N个引擎）、运维复杂

## Consequences

### 正面

- 跨租户流程在共享引擎内**天然支持**（同一引擎内流转）
- 跨租户待办查询**一条SQL搞定**（不加 tenantId 过滤）
- 资源成本低（1个引擎实例）
- 运维简单

### 负面

- 逻辑隔离而非物理隔离
- 大租户流程量可能影响其他租户

### 缓解措施

- 独立部署的子公司自带 Flowable + 独立库（天然物理隔离）
- 未来大租户可通过 `DataSourceBasedMultiTenantConnectionProvider` 切独立库

### 关键映射

```
"global"       → 集团级流程模板（所有租户可用）
"{tenantId}"   → 具体租户的流程实例/任务
```
