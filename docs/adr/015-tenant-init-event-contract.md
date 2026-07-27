# ADR-015: 租户初始化事件契约化

- **Status**: Accepted
- **Date**: 2026-07-27

## Context

租户创建后需初始化各模块数据（管理员/根部门/默认菜单/角色权限等），现靠 `TenantCreatedEvent` + 各 `*Initializer` 监听器。问题：
- 监听器用 `@EventListener`（非 `AFTER_COMMIT`）+ 复用外层 `createTenant` 事务会话，`@TenantId` 会话开启时捕获的租户为创建者租户，`TenantContext.switchTo(newTenantId)` 在会话开启后设置不生效 → 新数据可能写成**创建者租户**（复盘 S1，串租户）
- 初始化失败回滚整个 `createTenant`，强耦合
- 初始化职责未契约化文档化，各模块各自为政
- 新租户 admin 密码硬编码 `admin123` + 日志明文（复盘 S3）

## Decision

继续事件驱动，但**契约化 + 修时序 + 补偿化**，基于 ADR-014 事件基础设施：

### 1. 初始化契约清单（落设计文档）

`TenantCreatedEvent` 触发后，各模块声明初始化职责：

| 模块 | 初始化内容 |
|---|---|
| identity | 管理员用户（随机密码 + 首登强制改密）+ 默认角色 |
| organization | 根部门 + 默认岗位 |
| authorization | 默认菜单分配 + 角色权限绑定 |
| message/config | 默认消息模板/系统参数 |
| 其他模块 | 按产品定义补充 |

### 2. 时序修复

监听器实现 ADR-014 的 `DomainEventListener<TenantCreatedEvent>`，分发器统一：
- `@TransactionalEventListener(AFTER_COMMIT)` + `Propagation.REQUIRES_NEW`：在新事务/新会话执行
- `ActorContext.switchTo(newTenantId)` 在新会话开启前生效，`@TenantId` 正确捕获新租户

### 3. 幂等与补偿

- 每个初始化器按 `(tenantId, 初始化项)` 去重（显式带 tenantId 查询，不依赖隐式 `@TenantId` 过滤）
- AFTER_COMMIT 后初始化失败不回滚租户创建，记录 `tenant_init_status` 表，可单独重试补偿

### 4. 安全收敛

- 新租户密码随机生成 + 首登强制改密
- 日志不打印明文密码

## Alternatives

### 方案 B：在 `createTenant` 内同步直接调各模块初始化

- **优点**：无事件时序问题
- **缺点**：tenant 模块需依赖所有业务模块，耦合爆炸；失败全回滚

### 方案 C：改用 `@TransactionalEventListener(AFTER_COMMIT)` 但不抽象监听器

- **优点**：解决时序
- **缺点**：租户切换/幂等/重试仍各监听器各写各的，无统一治理（ADR-014 已决策统一抽象）

## Consequences

### 正面

- 串租户风险消除（新会话捕获新租户）
- 初始化失败可补偿，不回滚租户创建
- 契约清单文档化，各模块职责清晰
- 密码安全收敛

### 负面

- 依赖 ADR-014 事件消费端抽象（分发器提供 AFTER_COMMIT/REQUIRES_NEW/switchTo）
- `tenant_init_status` 表 + 补偿机制需开发

### 缓解措施

- 与 ADR-014 同批落地（消费端抽象是其底座）
- 一期契约清单覆盖核心模块（identity/org/auth），其他模块按需补充
