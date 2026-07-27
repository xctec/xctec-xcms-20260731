# ADR-013: 数据权限横切下沉与 data-rule 统一

- **Status**: Accepted
- **Date**: 2026-07-27

## Context

数据权限（行级数据范围 + 列级脱敏）当前都在 `xcms-auth-impl`：
- 行级：`DataPermissionService.getDataScopeSpec` 读 `DataRule` 生成 JPA Specification
- 列级：`ColumnMaskResponseBodyAdvice`（HTTP 出口切面）

微服务下若让所有服务依赖 auth-impl 才生效，违反模块边界（auth 不应被所有服务依赖）；不依赖则失效。脱敏是 HTTP 出口切面，跨服务/聚合层 fail-open（缺规则/tenantId 为 null 即裸奔），且耦合 `ApiResponse`（复盘 S13）。

此外，前端 Permission 页"数据范围"用臆造的 data-scope 模型（`ALL`/`SELF`/`CURRENT_DEPT`/`DEPT_AND_CHILD`/`CUSTOM` 枚举），后端实际是 data-rule（`OWNER`/`ORG`/`BUSINESS_LINE`/`REGION`/`TAG`/`TIME` 多维度规则），两者模型不对接，前端调臆造 `/admin/authz/data-scope*` 联调即 404。

## Decision

### 1. 数据权限横切下沉为独立薄模块

新建 `xcms-data-permission-api`（SPI/注解/契约）+ `xcms-data-permission-impl`（默认实现），仅依赖 shared-kernel。auth-impl 只管"规则 CRUD 管理"，**运行时拦截由 data-permission 提供**，所有服务天然具备，不依赖 auth-impl。

- **行级**：定义 SPI `DataPermissionRuleProvider`（按 `resourceType` 返回规则），各业务模块自行实现注册。auth-impl 的 `DataRuleServiceImpl` 实现该 SPI 作"管理面提供者"。拦截点为 Hibernate filter 或 Specification 包装器基类。
- **列级脱敏**：从 `ResponseBodyAdvice`（HTTP 出口、依赖标注、fail-open）**下沉到 DTO 映射层**——`@MaskField(resourceType, field)` 注解 + 序列化增强，`toDTO` 后立即脱敏，**fail-closed**（缺规则/tenantId 为 null 默认脱敏或拒绝）。解除 `ApiResponse` 耦合。
- **跨服务语义**：内部服务调用不脱敏（保留数据完整性），仅"面向最终用户的出网边界"脱敏，靠 ADR-012 的"系统调用"身份标识区分。
- **规则缓存**：抽 `CachePort`（见 ADR-014 SPI 原则），单体用 Caffeine，集群用 Redis，`PermissionChangedEvent` 经事件广播失效。

### 2. 数据权限模型统一为 data-rule

废弃前端臆造的 data-scope 接口，统一存储用 **data-rule**（后端 `perm_data_rule` + `perm_data_rule_role`，运行时 `DataPermissionService` 本就读 data-rule）。前端提供"快速模式（预设）+ 高级模式（编辑）"两种 UI：

| data-scope 预设 | 映射为 data-rule |
|---|---|
| `ALL` | 清除该角色所有规则（无规则=全量） |
| `SELF` | 生成 `OWNER` 规则（`ownerField=createdBy`） |
| `CURRENT_DEPT` | 生成 `ORG` 规则（当前部门 path） |
| `DEPT_AND_CHILD` | 生成 `ORG` 规则（本部门 path 前缀，`like ?%`） |
| `CUSTOM` | 展开 data-rule 多维度编辑器（ORG/BUSINESS_LINE/REGION/TAG/TIME） |

预设映射放前端（后端 data-rule API 已齐，零改动）。一期先做 4 预设覆盖 90% 场景，`CUSTOM` 多维度编辑器二期。

## Alternatives

### 方案 B：数据权限留在 auth-impl，所有服务依赖 auth-impl

- **优点**：零迁移
- **缺点**：违反模块边界，auth 被所有服务强依赖，拆分时耦合爆炸

### 方案 C：保留 data-scope 粗粒度模型，后端补 data-scope 端点

- **优点**：前端不动
- **缺点**：只能组织/本人维度，无法支持业务线/地区/标签/时间；放弃后端已实现的细粒度能力

## Consequences

### 正面

- 数据权限能力下沉，所有服务天然具备，不依赖 auth-impl
- 脱敏 fail-closed，安全基线提升；解除 HTTP 出口/`ApiResponse` 耦合
- data-rule 统一，消除前端臆造接口，复用后端细粒度引擎
- data-scope 预设保留简化体验，`CUSTOM` 提供复杂场景出口

### 负面

- 新建 data-permission 模块，auth-impl 职责重构有迁移成本
- 脱敏下沉 DTO 层需各模块 `toDTO` 配合注解
- `CUSTOM` 多维度编辑器 UI 复杂

### 缓解措施

- 迁移分步：先抽接口 + auth-impl 实现提供者，再各模块接入
- 脱敏注解 + MapStruct 增强自动化，降低手动配合
- data-scope 预设一期落地，`CUSTOM` 二期
