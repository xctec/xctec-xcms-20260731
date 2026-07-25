# XCMS Documentation

> XCMS - 集团中台，各产线的技术底座

## Document Index

### Product & Architecture

| Document | Description |
|----------|-------------|
| [01-overview.md](01-overview.md) | 产品概述：背景、定位、设计原则、术语表 |
| [02-architecture.md](02-architecture.md) | 总体架构：技术架构、部署架构、租户体系、管理面与业务面 |

### Core Modules (Phase 1)

| Document | Description |
|----------|-------------|
| [03-core-modules/](03-core-modules/) | 核心基础层索引 |
| [tenant-management.md](03-core-modules/tenant-management.md) | 租户管理：级联租户、配额、生命周期 |
| [organization.md](03-core-modules/organization.md) | 组织架构：部门、岗位、人员 |
| [identity.md](03-core-modules/identity.md) | 身份认证：用户、SSO、会话 |
| [authorization.md](03-core-modules/authorization.md) | 权限中心：RBAC+ABAC、数据权限、跨租户授权 |

### Shared Services (Phase 2)

| Document | Description |
|----------|-------------|
| [04-shared-services/](04-shared-services/) | 共享业务层索引 |
| [message-center.md](04-shared-services/message-center.md) | 消息中心：站内信、多渠道通知 |
| [configuration.md](04-shared-services/configuration.md) | 配置中心：参数、功能开关、字典 |
| [file-storage.md](04-shared-services/file-storage.md) | 文件存储：上传下载、预览、分享 |
| [task-scheduling.md](04-shared-services/task-scheduling.md) | 任务调度：定时任务、异步任务 |
| [audit.md](04-shared-services/audit.md) | 审计中心：操作审计、跨租户审计 |
| [workflow.md](03-core-modules/workflow.md) | 流程中心：Flowable BPM、跨租户流程（Phase 2 交付，设计见核心模块索引） |

### Operations & Portal (Phase 3)

| Document | Description |
|----------|-------------|
| [05-operations.md](05-operations.md) | 运营管理：看板、计量、监控告警 |
| [06-portal.md](06-portal.md) | XCMS 门户：统一入口、工作台 |
| [07-menu-specification.md](07-menu-specification.md) | 功能菜单总览：管理面 + 业务面 |

### Non-functional & Roadmap

| Document | Description |
|----------|-------------|
| [08-non-functional.md](08-non-functional.md) | 非功能性需求：性能、安全、高可用、扩展性 |
| [09-roadmap.md](09-roadmap.md) | 实施路线：4阶段规划与里程碑 |

### Architecture Decision Records (ADR)

| ADR | Decision | Status |
|-----|----------|--------|
| [001](adr/001-modular-monolith.md) | 采用模块化单体而非微服务 | Accepted |
| [002](adr/002-single-db-multi-tenant.md) | 单库 + tenant_id 多租户隔离 | Accepted |
| [003](adr/003-layered-management-plane.md) | 分层管理面（方案C） | Accepted |
| [004](adr/004-api-module-pattern.md) | API/Impl 模块拆分模式 | Accepted |
| [005](adr/005-flowable-shared-engine.md) | Flowable 共享引擎 | Accepted |
| [006](adr/006-cross-tenant-workflow.md) | 跨租户流程：归属权与参与权分离 | Accepted |
| [007](adr/007-independent-deployment.md) | 独立部署不互通业务数据 | Accepted |
| [008](adr/008-jpa-tenant-id.md) | JPA @TenantId 多租户 | Accepted |
| [009](adr/009-mapstruct-entity-mapping.md) | MapStruct Entity ↔ DTO 转换 | Accepted |
| [010](adr/010-all-post-api-style.md) | 全 POST API 风格 | Accepted |

### Appendix

| Document | Description |
|----------|-------------|
| [tech-stack.md](appendix/tech-stack.md) | 技术栈汇总 |
| [module-list.md](appendix/module-list.md) | 模块清单 |
| [glossary.md](appendix/glossary.md) | 术语表 |
