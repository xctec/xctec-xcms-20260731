# Architecture Decision Records (ADR)

> 记录 XCMS 架构决策的背景、选择和理由

## Index

| # | Decision | Status |
|---|----------|--------|
| [001](001-modular-monolith.md) | 采用模块化单体而非微服务 | Accepted |
| [002](002-single-db-multi-tenant.md) | 单库 + tenant_id 多租户隔离 | Accepted |
| [003](003-layered-management-plane.md) | 分层管理面架构 | Accepted |
| [004](004-api-module-pattern.md) | API/Impl 模块拆分模式 | Accepted |
| [005](005-flowable-shared-engine.md) | Flowable 共享引擎 | Accepted |
| [006](006-cross-tenant-workflow.md) | 跨租户流程：归属权与参与权分离 | Accepted |
| [007](007-independent-deployment.md) | 独立部署不互通业务数据 | Accepted |
| [008](008-jpa-tenant-id.md) | JPA @TenantId 多租户 | Accepted |
| [009](009-mapstruct-entity-mapping.md) | MapStruct Entity ↔ DTO 转换 | Accepted |
| [010](010-all-post-api-style.md) | 全 POST API 风格 | Accepted |
| [011](011-spring-security.md) | 鉴权体系迁移 Spring Security | Accepted |
| [012](012-stateless-service-and-inter-service-call.md) | 无状态服务与微服务调用规范 | Accepted |
| [013](013-data-permission-sink-and-data-rule.md) | 数据权限横切下沉与 data-rule 统一 | Accepted |
| [014](014-event-driven-listener-and-outbox.md) | 事件驱动消费端抽象与事务性 Outbox | Accepted |
| [015](015-tenant-init-event-contract.md) | 租户初始化事件契约化 | Accepted |
| [016](016-async-task-multi-instance.md) | 异步任务多实例就绪（降优先级，暂缓） | Accepted |
| [017](017-observability-prometheus.md) | 可观测性采用 Prometheus + Grafana + Alertmanager | Accepted |
| [018](018-realtime-push-sse.md) | 实时推送采用 SSE + Redis pub/sub | Accepted |

## ADR Format

Each ADR follows this structure:

- **Title**: Decision name
- **Status**: Accepted / Proposed / Deprecated / Superseded
- **Context**: Background and problem statement
- **Decision**: What was decided
- **Alternatives**: What else was considered
- **Consequences**: Implications of the decision
