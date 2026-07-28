# 架构调整开发任务书

> 基于 ADR-011 ~ ADR-018，拆分为可执行开发任务，作为开发基准。
> 日期：2026-07-27
> 原则：单体可用优先，微服务就绪项标注「拆分前」延后；每个任务含需求细节、关键机制、涉及模块、验收标准。

## 一、总览

### 批次与迭代

| 迭代 | 主题 | 任务 | 周期 | 目标 |
|---|---|---|---|---|
| 迭代一 | 事件底座 + 安全基线 | AT-01~03, AT-06~07, AT-15 | 2 周 | 消费端抽象落地、Spring Security 接管鉴权、密码安全 |
| 迭代二 | 身份规范 + 租户初始化 | AT-04, AT-08~09, AT-11~14 | 2 周 | ActorContext、方法级权限、服务令牌、初始化时序修复 |
| 迭代三 | 数据权限 + 推送 + 监控 | AT-16~21, AT-23~27 | 3 周 | data-permission 下沉、SSE 推送、Prometheus 栈 |
| 拆分前 | 微服务就绪 | AT-05, AT-10, AT-22, AT-28, AT-29 | 按需 | Outbox、出站传播、Redis 广播、任务多实例、web-starter 抽取 |

### 完成情况（截至 2026-07-28，核实 master 实际代码）

| 任务 | 状态 | 说明 |
|---|---|---|
| AT-01 | ✅ 已完成 | 事件端口+分发器（master） |
| AT-02 | ✅ 已完成 | AFTER_COMMIT 分发（master） |
| AT-03 | ✅ 已完成 | EventDedupPort 幂等（master） |
| AT-04 | ✅ 已完成 | ActorContext+TaskDecorator（master） |
| AT-05 | ❌ 未开始 | Outbox+MQ（可单体预留：SPI+NoOp 默认，拆分切换） |
| AT-06 | ✅ 已完成 | SecurityConfig+JWT RS（master） |
| AT-07 | ✅ 已完成 | 拦截器退化（master） |
| AT-08 | ⚠️ 部分完成 | @PreAuthorize 仅 4 处，需逐模块核心写操作继续补齐 |
| AT-09 | ✅ 已完成 | ActorContext 薄包装（master，30+ 调用点待全量迁移收尾） |
| AT-10 | ❌ 未开始 | 出站传播（可单体预留：拦截器+条件装配，单体不启用） |
| AT-11 | ✅ 已完成 | 服务令牌全链路（master） |
| AT-12 | ✅ 已完成 | refresh cookie+CSRF 双保险（master） |
| AT-13 | ✅ 已完成 | REQUIRES_NEW 时序修复（master） |
| AT-14 | ✅ 已完成 | tenant_init_status 补偿（master） |
| AT-15 | ✅ 已完成 | 随机密码+首登改密（master） |
| AT-16 | ✅ 已完成 | data-permission 模块下沉（master） |
| AT-17 | ✅ 已完成 | 行级 SPI+查询基类（master） |
| AT-18 | ✅ 已完成 | @MaskField 脱敏下沉+fail-closed（master） |
| AT-19 | ✅ 已完成 | 前端 data-rule 对接（master） |
| AT-20 | ✅ 已完成 | SSE 端点+连接管理（master） |
| AT-21 | ✅ 已完成 | 推送监听器（master） |
| AT-22 | ❌ 未开始 | SSE Redis 广播（可单体预留：PubSubPort+NoOp，单体直接推送） |
| AT-23 | ❌ 未开始 | 前端 useNotification hook（单体必做，SSE 闭环） |
| AT-24 | ✅ 已完成 | Micrometer+Prometheus 暴露（已合并 master） |
| AT-25 | ✅ 已完成 | MetricProvider 改注册 Meter（已合并 master） |
| AT-26 | ✅ 已完成 | 废弃 metric_value 落库（已合并 master；**P1 残留：AlertRule.getLatest 读旧表告警失效，待 AT-27 接管**） |
| AT-27 | ❌ 未开始 | Grafana+Alertmanager（AT-26 P1 依赖此项，单体可做） |
| AT-28 | ❌ 未开始 | 任务多实例（暂缓，优先级最低） |
| AT-29 | ❌ 未开始 | web 基础设施抽取为独立 starter（消除 4 impl 重复 security 依赖 + 配置复用；单体可做，拆分复用） |

**进度小结**：迭代一 ✅ 全完成；迭代二 ⚠️ 基本完成（AT-08 需继续补）；迭代三 数据权限+推送链 ✅ 完成，监控采集（AT-24~26）✅ 已完成，**AT-23（前端 hook）/AT-27（Grafana+告警接管）未启动**（AT-26 P1 待 AT-27 解决）；拆分前项（AT-05/10/22/28/29）可按"单体预留实现"模式先行（SPI+条件装配，单体零成本，拆分切换）；AT-29（web-starter 抽取）单体即可做，消除 4 impl 重复依赖。

### 依赖关系

```
AT-01(消费端抽象) ─┬─ AT-02(监听器迁移) ─ AT-13(初始化时序)
                   ├─ AT-03(eventId幂等)
                   ├─ AT-04(@Async传播) ← AT-09(ActorContext)
                   └─ AT-21(推送监听器) ← AT-20(SSE端点)
AT-06(SecurityFilterChain) ─┬─ AT-07(拦截器退化)
                            ├─ AT-08(@PreAuthorize)
                            ├─ AT-09(ActorContext) ─ AT-10(出站传播,拆分前) ← AT-11(服务令牌)
                            ├─ AT-11(服务令牌)
                            └─ AT-12(refresh cookie)
AT-09(ActorContext) ─ AT-16(data-permission模块) ─┬─ AT-17(行级SPI)
                                                  └─ AT-18(列级脱敏) → AT-19(前端data-rule对接)
AT-20(SSE) ─ AT-21(推送监听器) ─ AT-22(Redis广播,拆分前)
AT-24(Micrometer) ─┬─ AT-25(Meter注册) ─ AT-26(废弃落库)
                   └─ AT-27(Grafana+Alertmanager)
```

---

## 二、迭代一：事件底座 + 安全基线

### AT-01: 事件消费端端口与统一分发器

- **所属 ADR**：ADR-014
- **依赖**：无（底座）
- **需求细节**：在 `xcms-shared-kernel` 的 event 包新增入站事件端口与统一分发器，使发布/消费对称。现有 5 个监听器（`AuditEventListener`/`TaskFailedEventListener`/`PermissionCacheEvictor`/`TenantUserInitializer`/`TenantOrgInitializer`）改为实现该端口。
- **关键机制**：
  1. 定义 `DomainEventListener<E extends DomainEvent>`：`void onEvent(E event); Class<E> eventType();`
  2. 定义 `DomainEventDispatcher`：收集所有 `DomainEventListener` Bean，按 `eventType()` 路由。进程内由单个 `@EventListener(DomainEvent.class)` 方法委托分发器。
  3. **横切集中到分发器**（业务监听器只写 `onEvent` 纯逻辑）：
     - 进入 `onEvent` 前调 `TenantContext.switchTo(event.getTenantId())`（消除各监听器手动切换的不一致）
     - `eventId` 幂等去重（见 AT-03）
     - 异常捕获 + 日志（不吞，交重试机制）
  4. 分发器可单测（无需 Spring 事件总线）。
- **涉及模块/文件**：`xcms-shared-kernel/.../event/`（新增 `DomainEventListener.java`、`DomainEventDispatcher.java`）
- **验收标准**：5 个监听器改为实现 `DomainEventListener`；分发器统一做租户切换；监听器内不再手动 `switchTo`；`mvn test` 通过。

### AT-02: 监听器改 AFTER_COMMIT + 经分发器

- **所属 ADR**：ADR-014
- **依赖**：AT-01
- **需求细节**：5 个监听器从裸 `@EventListener` 改为 `@TransactionalEventListener(AFTER_COMMIT)`，事务提交后才处理，避免业务回滚仍发事件/丢事件。
- **关键机制**：
  1. 监听器实现 `DomainEventListener`，去掉 `@EventListener` 注解，由分发器统一调度。
  2. `PermissionCacheEvictor` 已是 `AFTER_COMMIT`，保留并接入分发器。
  3. `AuditEventListener`/`TaskFailedEventListener`/`TenantUserInitializer`/`TenantOrgInitializer` 从 `@EventListener` 改经分发器（分发器内部用 `AFTER_COMMIT` 触发）。
- **涉及模块**：`xcms-auth-impl`（PermissionCacheEvictor）、`xcms-audit-impl`（AuditEventListener）、`xcms-task-impl`（TaskFailedEventListener）、`xcms-tenant-impl`（两个 Initializer）
- **验收标准**：业务事务回滚时事件不处理；监听器内无 `@EventListener` 注解；租户切换由分发器统一完成。

### AT-03: eventId 幂等去重

- **所属 ADR**：ADR-014
- **依赖**：AT-01
- **需求细节**：分发器处理事件前按 `eventId` 去重，防 at-least-once 重复投递导致重复副作用（重复初始化、重复审计、重复推送）。
- **关键机制**：
  1. `DomainEvent` 已有 `eventId()`（确认存在，否则补）。
  2. 去重存储抽 `EventDedupPort` 端口：单体用本地 `ConcurrentHashMap`/Caffeine（TTL 24h），集群用 Redis `SETNX`（`@ConditionalOnProperty` 切换）。
  3. 分发器 `onEvent` 前 `if (!dedupPort.tryAcquire(eventId)) return;`。
- **涉及模块**：`xcms-shared-kernel/.../event/`（`EventDedupPort`、本地实现）
- **验收标准**：同一 `eventId` 重复投递只处理一次；去重实现可切换（本地/Redis）。

### AT-06: SecurityFilterChain + JWT Resource Server

- **所属 ADR**：ADR-011
- **依赖**：无（底座）
- **需求细节**：引入 Spring Security 6，配置 JWT Resource Server，由 Spring Security 校验 JWT 签名/exp/claims，替代 `TenantInterceptor` 的 token 校验职责。
- **关键机制**：
  1. `xcms-app` 加 `spring-boot-starter-oauth2-resource-server`。
  2. `SecurityFilterChain`：`oauth2ResourceServer().jwt()`，`JwtAuthenticationConverter` 从 JWT claim 构建 `GrantedAuthority`（权限码）。
  3. 权限来源：登录时把用户权限码写入 JWT claim（或 `JwtAuthenticationConverter` 调 `PermissionService` 取，配合缓存）。
  4. `AuthenticationEntryPoint`/`AccessDeniedHandler` 统一返回 `ApiResponse{errorCode,errorMsg}` 契约（修复 H1/H2 错误契约不一致）。
  5. 放行路径：`/api/auth/login`、`/api/auth/refresh`、`/api/tenant/lookup`、`/api/sso/**`。
- **涉及模块**：`xcms-app`（SecurityConfig）、`xcms-identity-impl`（JwtTokenProvider 协同）
- **验收标准**：无 token/过期 token 返回 401 `ApiResponse`；权限不足返回 403 `ApiResponse`；登录/租户查找接口免鉴权。

### AT-07: TenantInterceptor 退化为上下文填充

- **所属 ADR**：ADR-011
- **依赖**：AT-06
- **需求细节**：`TenantInterceptor` 不再校验 token（由 Spring Security 完成），仅从已认证 `Authentication` 提取 tenantId/userId 写入 `TenantContext`；删除手写 401 JSON（修复 H2）。
- **关键机制**：
  1. 拦截器 `preHandle` 从 `SecurityContextHolder.getContext().getAuthentication()` 取 `Jwt` claim 的 tenantId/userId，`TenantContext.set(...)`。
  2. 删除 `writeUnauthorized`（401 交 `AuthenticationEntryPoint`）。
  3. 拦截器仅做"是否放行"判定与上下文填充。
- **涉及模块**：`xcms-portal/.../web/TenantInterceptor.java`
- **验收标准**：token 校验由 Spring Security 完成；拦截器只填 TenantContext；401 响应为标准 `ApiResponse`。

### AT-15: 新租户密码随机化 + 首登改密

- **所属 ADR**：ADR-015（安全收敛）
- **依赖**：无
- **需求细节**：`TenantUserInitializer` 新租户 admin 密码从硬编码 `admin123` 改为随机生成；日志不打印明文；首登强制改密。
- **关键机制**：
  1. 随机密码：`SecureRandom` 生成 12 位含大小写+数字+符号。
  2. 日志只记"初始密码已生成，请联系管理员"，不打印明文；密码经安全渠道下发（或登录后展示一次性）。
  3. `User.passwordChangedAt == null` 时 `LoginResult.forceChangePassword=true`（已有字段），前端登录后跳改密页。
- **涉及模块**：`xcms-tenant-impl/.../TenantUserInitializer.java`、`xcms-front`（改密页已有则对接）
- **验收标准**：新租户密码不硬编码；日志无明文；首登强制改密。

---

## 三、迭代二：身份规范 + 租户初始化

### AT-04: @EnableAsync + TaskDecorator 传播 ActorContext

- **所属 ADR**：ADR-012 / ADR-014
- **依赖**：AT-09（ActorContext）
- **需求细节**：启用 `@EnableAsync`，异步线程池配 `TaskDecorator` 显式传播 `ActorContext`，解决 ThreadLocal 跨线程丢失。
- **关键机制**：
  1. `@EnableAsync` + 自定义 `TaskExecutor`，`TaskDecorator` 在提交任务时捕获主线程 `ActorContext`，在子线程 `set` 后执行，`finally` `clear`。
  2. `@Async` 监听器/任务自动继承租户与用户上下文。
- **涉及模块**：`xcms-app`（AsyncConfig）
- **验收标准**：`@Async` 方法内 `ActorContext.getTenantId()`/`getCurrentUserId()` 非空且正确。

### AT-08: 方法级权限 @PreAuthorize 逐模块补齐

- **所属 ADR**：ADR-011
- **依赖**：AT-06
- **需求细节**：在关键写操作 Controller 批量补 `@PreAuthorize("hasAuthority('xxx:yyy')")`，后端强制校验，不依赖前端 `<Can>`。
- **关键机制**：
  1. 权限码与菜单按钮项 `permission` 字段对齐（如 `user:create`/`tenant:delete`/`role:permission`）。
  2. 分批：先 identity/tenant/org/role/permission 核心写操作，再 workflow/message/file/audit。
  3. 查询接口可放宽（登录即可）或加 `:view` 权限码。
- **涉及模块**：各 `*Controller.java`
- **验收标准**：无权限用户调写操作返回 403；权限码与前端 `Can` 一致。

### AT-09: TenantContext 重命名为 ActorContext

- **所属 ADR**：ADR-012
- **依赖**：AT-06
- **需求细节**：`TenantContext` 实为"调用者身份上下文"，更名 `ActorContext`，拆 `tenantId` + `principal{userId/serviceName}`，删除死字段 `dataSourceKey`。
- **关键机制**：
  1. 新建 `ActorContext`（ThreadLocal），含 `tenantId` + `principal`（userId 或 service principal）。
  2. `TenantContext` 暂保留为 `ActorContext` 的薄包装（`getTenantId` 委托），逐处迁移后删除。
  3. `getCurrentUserId()` 改 `getPrincipal().userId()`；系统触发场景（调度/MQ）注入 service principal（`system@xxx`），不再 null。
  4. 删除 `dataSourceKey`（全仓恒为 `shared`，无 `AbstractRoutingDataSource` 消费）；`set` 默认值同步移除。
- **涉及模块**：`xcms-shared-kernel/.../context/`（新 `ActorContext`）、全仓 `TenantContext` 调用点（135+ 处，分批迁移）
- **验收标准**：`ActorContext` 承载 tenantId+principal；`dataSourceKey` 移除；系统触发场景 principal 非 null。

### AT-11: 服务令牌签发

- **所属 ADR**：ADR-012
- **依赖**：AT-06
- **需求细节**：为调度/MQ 消费者/服务间调用等无用户态的触发源签发服务令牌（service account JWT，含 tenantId + service principal）。
- **关键机制**：
  1. 复用 `JwtTokenProvider`，新增 `issueServiceToken(tenantId, serviceName, ttl)`，claim 标记 `token_type=service`。
  2. Spring Security `JwtAuthenticationConverter` 识别 service token，构建 `service principal` 权限。
  3. 调度引擎/MQ 消费者启动时获取服务令牌，调用业务接口时携带。
- **涉及模块**：`xcms-identity-impl`（JwtTokenProvider 扩展）、`xcms-task-impl`（调度触发）
- **验收标准**：服务令牌可签发；Spring Security 识别 service principal；调度任务调业务接口不被 401。

### AT-12: refresh token 改 httpOnly cookie

- **所属 ADR**：ADR-012
- **依赖**：AT-06
- **需求细节**：refresh token 从 localStorage 移到 httpOnly cookie，降低 XSS 窃取长效凭证风险。
- **关键机制**：
  1. 登录/刷新接口 `Set-Cookie: refresh_token=...; HttpOnly; Secure; SameSite=Strict; Path=/api/auth`。
  2. 前端 `stores/auth` persist 用 `partialize` 排除 `refreshToken`（只 persist access token + 用户信息）。
  3. 刷新接口从 cookie 读 refresh token（不前端传 body）。
  4. 前端不再存/读 refreshToken。
- **涉及模块**：`xcms-identity-impl`（AuthController login/refresh）、`xcms-front`（http.ts、stores/auth.ts）
- **验收标准**：refresh token 不在 localStorage/JS 可读；刷新功能正常；XSS 无法窃取 refresh token。

### AT-13: 租户初始化器时序修复

- **所属 ADR**：ADR-015
- **依赖**：AT-01, AT-02
- **需求细节**：修复 `TenantUserInitializer`/`TenantOrgInitializer` 的 `@TenantId` 串租户风险——监听器在新事务/新会话执行，`switchTo(newTenantId)` 在会话开启前生效。
- **关键机制**：
  1. 监听器经 AT-01 分发器调度，分发器 `AFTER_COMMIT` + `Propagation.REQUIRES_NEW` 新事务。
  2. 分发器在 `switchTo(event.getTenantId())` 后再调 `onEvent`，新会话开启时 `@TenantId` 捕获新租户。
  3. 初始化器内查询显式带 tenantId（不依赖隐式 `@TenantId` 过滤，如 `findByUsername` 改 `findByTenantIdAndUsername`）。
- **涉及模块**：`xcms-tenant-impl`（两个 Initializer）、`xcms-shared-kernel`（分发器 REQUIRES_NEW）
- **验收标准**：新租户初始化数据的 tenant_id 为新租户而非创建者租户；幂等查询显式带 tenantId。

### AT-14: tenant_init_status 补偿表

- **所属 ADR**：ADR-015
- **依赖**：AT-13
- **需求细节**：AFTER_COMMIT 后初始化失败不回滚租户创建，记录 `tenant_init_status` 表，可单独重试补偿。
- **关键机制**：
  1. 新表 `tenant_init_status`（tenant_id, module, status, error_msg, retry_count, updated_at）。
  2. 每个初始化器 try/catch，失败写 `tenant_init_status`（status=FAILED），不抛出。
  3. 提供补偿接口/任务：查 FAILED 记录，重新触发对应初始化器。
- **涉及模块**：`xcms-tenant-impl`（新增表 + 补偿服务）
- **验收标准**：初始化失败不回滚租户；失败记录可查可重试。

---

## 四、迭代三：数据权限 + 推送 + 监控

### AT-16: 新建 xcms-data-permission 模块

- **所属 ADR**：ADR-013
- **依赖**：AT-09
- **需求细节**：新建 `xcms-data-permission-api`（SPI/注解/契约）+ `xcms-data-permission-impl`（默认实现），仅依赖 shared-kernel，使数据权限能力下沉，所有服务天然具备，不依赖 auth-impl。
- **关键机制**：
  1. `xcms-data-permission-api`：定义 `DataPermissionService`（getDataScopeSpec/applyColumnMask）、`DataPermissionRuleProvider`（SPI）、`@MaskField` 注解、`DataPermissionContext`。
  2. `xcms-data-permission-impl`：默认实现，规则缓存抽 `CachePort`（单体 Caffeine，集群 Redis，`@ConditionalOnProperty`）。
  3. auth-impl 的 `DataPermissionServiceImpl` 逻辑迁入 data-permission-impl；auth-impl 只保留 `DataRuleService`（规则 CRUD 管理）。
  4. 各业务服务依赖 data-permission-api 即获得行级过滤 + 列级脱敏能力。
- **涉及模块**：新建 `xcms-data-permission/`、`xcms-app` pom 装配
- **验收标准**：业务服务依赖 data-permission-api 即具备数据权限；auth-impl 不再被业务服务为数据权限而依赖。

### AT-17: 行级数据权限 SPI + auth 实现提供者

- **所属 ADR**：ADR-013
- **依赖**：AT-16
- **需求细节**：行级数据范围通过 SPI `DataPermissionRuleProvider`（按 resourceType 返回规则）实现，各业务模块自行注册，auth-impl 作"管理面提供者"。
- **关键机制**：
  1. `DataPermissionRuleProvider`：`List<DataRule> getRules(String resourceType, Long tenantId)`。
  2. auth-impl 的 `DataRuleServiceImpl` 实现该 SPI（从 `perm_data_rule` 查）。
  3. `DataPermissionService.getDataScopeSpec` 从所有 `DataPermissionRuleProvider` 汇总规则 → Specification（现有 `DataPermissionServiceImpl` 逻辑迁移）。
  4. 业务 repository 继承提供基类或用 `getDataScopeSpec` 包装查询。
- **涉及模块**：`xcms-data-permission-api/impl`、`xcms-auth-impl`
- **验收标准**：行级过滤按 resourceType + 角色规则生效；业务模块可自定义 Provider。

### AT-18: 列级脱敏下沉 DTO 层 + fail-closed

- **所属 ADR**：ADR-013
- **依赖**：AT-16
- **需求细节**：脱敏从 `ColumnMaskResponseBodyAdvice`（HTTP 出口、依赖标注、fail-open）下沉到 DTO 映射层，`@MaskField` 注解 + `toDTO` 后脱敏，fail-closed。
- **关键机制**：
  1. `@MaskField(resourceType, field)` 标注 DTO 字段。
  2. 脱敏在 `toDTO` 后（MapStruct 增强或序列化增强）执行，调 `DataPermissionService.applyColumnMask`。
  3. **fail-closed**：缺规则/tenantId 为 null 时默认脱敏或拒绝，不裸奔（修复 S13 fail-open）。
  4. 解除 `ApiResponse` 耦合，所有返回类型生效。
  5. 内部服务调用不脱敏（靠 AT-11 service principal 区分），仅出网脱敏。
- **涉及模块**：`xcms-data-permission-api/impl`、各模块 DTO/MapStruct mapper
- **验收标准**：脱敏在 DTO 层生效；缺规则 fail-closed；`ApiResponse` 耦合解除。

### AT-19: 前端 data-scope 对接 data-rule（预设映射）

- **所属 ADR**：ADR-013
- **依赖**：AT-17
- **需求细节**：前端 Permission 页"数据范围"从臆造 `/admin/authz/data-scope*` 改对接真实 `/admin/data-rule/*`，data-scope 作预设 UI 映射为 data-rule。
- **关键机制**：
  1. `api/authorization.ts`：删 `getDataScope`/`updateDataScope`，加 `dataRuleApi`（list/bind/unbind，对接 `/admin/data-rule/*`）。
  2. `Permission.tsx` 数据范围区块：RadioGroup 选预设 → 前端映射为 data-rule 规则：
     - `ALL` → unbind 该角色所有规则
     - `SELF` → createRule(OWNER) + bind
     - `CURRENT_DEPT` → createRule(ORG, 当前部门 path) + bind
     - `DEPT_AND_CHILD` → createRule(ORG, 部门 path 前缀) + bind
     - `CUSTOM` → 二期展开维度编辑器
  3. 一期做 4 预设，`CUSTOM` 二期。
  4. mock 同步 `/admin/data-rule/*`。
- **涉及模块**：`xcms-front`（api/authorization.ts、Permission.tsx、mocks/handlers.ts）
- **验收标准**：选预设后角色数据范围生效（后端按 data-rule 过滤）；无臆造接口。

### AT-20: SSE 推送端点

- **所属 ADR**：ADR-018
- **依赖**：无
- **需求细节**：后端建立 SSE 推送通道 `/api/notifications/stream`，按 tenantId+userId 订阅。
- **关键机制**：
  1. Spring MVC `SseEmitter`，Controller 建 `SseEmitter`（超时 0 长连），按 `ActorContext` 的 tenantId+userId 注册到本地 `ConcurrentHashMap<key, SseEmitter>`。
  2. 心跳保活：定时发 `:heartbeat` 注释行防代理超时。
  3. 断线清理：`SseEmitter.onCompletion/onTimeout/onError` 移除注册。
- **涉及模块**：`xcms-portal` 或 `xcms-message`（NotificationController）
- **验收标准**：前端 `EventSource` 可连上；连接按租户+用户隔离；心跳保活。

### AT-21: 推送监听器（事件驱动）

- **所属 ADR**：ADR-018
- **依赖**：AT-01, AT-20
- **需求细节**：业务事件（MessageCreated/TodoAssigned/AlertFired）经 ADR-014 分发器 → 推送监听器 → 查 SSE 连接 → 推送。
- **关键机制**：
  1. 实现 `DomainEventListener` 订阅通知类事件。
  2. `onEvent` 从本地连接表查 `tenantId+userId` 的 `SseEmitter`，`send(SseEventBuilder)` 推送。
  3. 消息落库与推送解耦：落库由 MessageService，推送由事件驱动。
- **涉及模块**：`xcms-message`（推送监听器）、`xcms-portal`（SSE 连接表共享）
- **验收标准**：业务事件触发后在线用户收到 SSE 推送。

### AT-23: 前端 useNotification hook

- **所属 ADR**：ADR-018
- **依赖**：AT-20
- **需求细节**：前端封装 `useNotification()` hook，订阅 SSE，消息中心/待办角标实时更新。
- **关键机制**：
  1. `EventSource` 订阅 `/api/notifications/stream`，自动重连（`onerror` 重建）。
  2. 降级轮询兜底（SSE 失败 N 次后改 `setInterval` 拉通知）。
  3. 收到推送更新角标/消息列表（与 TanStack Query invalidate 协同）。
- **涉及模块**：`xcms-front`（hooks/useNotification.ts、消息中心/待办角标组件）
- **验收标准**：在线时实时收到通知；断线自动重连/降级轮询。
- **实现性质**：单体必做（非预留）。与单体/拆分无关，是 SSE 推送闭环（后端 AT-20/21 已就绪），单体就必须落地消费端，否则推送能力空转。

### AT-24: Micrometer + Prometheus 暴露

- **所属 ADR**：ADR-017
- **依赖**：无
- **需求细节**：加 `micrometer-registry-prometheus`，暴露 `/actuator/prometheus`，JVM/缓存指标自动暴露。
- **关键机制**：
  1. `xcms-app` 加依赖，`management.endpoints.web.exposure.include=prometheus,health,metrics`。
  2. `/actuator/prometheus` 可访问，含 JVM/缓存指标。
- **涉及模块**：`xcms-app`（pom + application.yml）
- **验收标准**：`/actuator/prometheus` 返回 Prometheus 格式指标。

### AT-25: MetricProvider 改注册 Meter

- **所属 ADR**：ADR-017
- **依赖**：AT-24
- **需求细节**：`MetricProvider` SPI 语义从"落 DB"改为"向 Micrometer 注册 Meter"，业务指标进 Prometheus。
- **关键机制**：
  1. `MetricProvider.registerMeters(MeterRegistry)`（新方法），各实现向 registry 注册 Counter/Gauge/Timer。
  2. 保留 `collectMetrics()` 作自定义聚合兜底（可选）。
- **涉及模块**：`xcms-operation`（MetricProvider SPI 及实现）
- **验收标准**：业务指标经 Micrometer 暴露在 `/actuator/prometheus`。

### AT-26: 废弃 metric_value 落库

- **所属 ADR**：ADR-017
- **依赖**：AT-25
- **需求细节**：移除 `MetricServiceImpl.collect()` 的 `metric_value` 落库逻辑。
- **关键机制**：
  1. `collect()` 不再 `save` 到 `metric_value`；指标由 Prometheus 拉取。
  2. 保留 `metric_value` 表一段时间作过渡（或迁移历史到 Prometheus），后续删表。
- **涉及模块**：`xcms-operation`（MetricServiceImpl）
- **验收标准**：指标不再写 `metric_value`；Prometheus 拉取替代。

### AT-27: Grafana 看板 + Alertmanager

- **所属 ADR**：ADR-017
- **依赖**：AT-24
- **需求细节**：Grafana 接 Prometheus 数据源替代静态 mock 看板；Alertmanager 接 PromQL 告警。
- **关键机制**：
  1. Grafana 仪表盘（JVM/业务指标，多租户 tenant label 过滤）。
  2. Alertmanager：PromQL + `for: 5m` 持续 + 分组/抑制/静默 + 多通道（邮件/钉钉/Webhook）。
  3. `AlertRule` 表改消费 Alertmanager webhook 归档 `alert_record`，不自算阈值（`durationMin` 生效）。
  4. 前端运营看板嵌 Grafana 或用 Grafana 数据源替代 mock（解决 F6）。
  5. **解决 AT-26 P1**：`AlertRuleServiceImpl.evaluate` 停用（不再读 `getLatest` 旧表），告警判定交 PromQL；`AlertRule` 表改消费 Alertmanager webhook 归档 `alert_record`，`durationMin` 终于生效。
- **涉及模块**：运维部署（Grafana/Alertmanager）、`xcms-operation`（AlertRule 改造）、`xcms-front`（看板）
- **验收标准**：Grafana 看板展示实时指标；告警持续 N 分钟触发；`durationMin` 生效。
- **实现性质**：单体可做。运维部署与单体/拆分无关（抓 `/actuator/prometheus`）；且**必须尽快做**以解决 AT-26 已合并到 master 的 P1（自建告警 `getLatest` 读旧表失效）。

---

## 五、拆分前：微服务就绪（按需，非当前迭代）

### AT-05: 事务性 Outbox + MQ 装配

- **所属 ADR**：ADR-014
- **依赖**：AT-01
- **需求细节**：事件随业务事务落 `event_outbox` 表（同库同事务），后台调度轮询转发 MQ，保证 at-least-once；`MqEventPublisher` 装配为 `@ConditionalOnProperty`。
- **关键机制**：
  1. `event_outbox` 表（eventId, topic, payload, status, retry_count, created_at）。
  2. `DomainEventPublisher` 发布时同时写 outbox（同事务）。
  3. 后台转发器轮询 outbox，调 `MqEventPublisher` 发 MQ，成功标记 SENT。
  4. MQ 选型（Kafka/RabbitMQ）此时确定，配齐 serializer/sender。
  5. 重试 + 死信队列。
- **涉及模块**：`xcms-shared-kernel`（outbox 表 + 转发器）、`xcms-app`（MQ 配置）
- **验收标准**：业务回滚事件不发出；崩溃不丢事件；MQ at-least-once。
- **实现模式（单体预留，可单体可拆分）**：
  - `OutboxEventPort` SPI + `NoOpOutboxEventPort`（`@ConditionalOnProperty("xcms.event.outbox.enabled", matchIfMissing=true)`）——单体不写表，事件经 `InProcessEventPublisher` AFTER_COMMIT 直接分发，零成本
  - `JpaOutboxEventPort` + `OutboxRelay`（`@ConditionalOnProperty("xcms.event.outbox.enabled", true)`）——拆分时启用，写表 + 后台转发 MQ
  - DDL 预留 `db/ddl/`（拆分时建表）；`MqEventPublisher` 骨架预留（MQ 选型未定先留接口）
  - 业务代码零改动（`DomainEventPublisher` 端口不变，实现切换）

### AT-10: 出站上下文传播拦截器

- **所属 ADR**：ADR-012
- **依赖**：AT-09, AT-11
- **需求细节**：跨服务 HTTP 调用统一注入上下文（用户 JWT 转发 或 服务令牌 + 内部签名头）。
- **关键机制**：
  1. `ClientHttpRequestInterceptor`/`RequestInterceptor`/`ExchangeFilterFunction`。
  2. 用户请求转发原 JWT；系统调用注入 service token + `X-Tenant-Id`/`X-User-Id` 签名头。
  3. 目标服务入口支持两种解析。
- **涉及模块**：`xcms-shared-kernel`（出站拦截器）
- **验收标准**：跨服务调用不断链；系统调用可被目标服务鉴权。
- **实现模式（单体预留，可单体可拆分）**：
  - `ActorContextPropagatingInterceptor`（`@ConditionalOnProperty("xcms.inter-service.enabled", matchIfMissing=true 关闭)`）——单体不装配，零成本（无跨服务调用）
  - 拆分时启用，RestTemplate/Feign/WebClient 加拦截器；用户请求转发原 JWT，系统调用注入 service token + `X-Tenant-Id`/`X-User-Id` 签名头
  - `ServiceTokenClient` 预留（复用 AT-11 `ServiceTokenService`）
  - 入口解析：目标服务 filter 预留识别 `token_type=service` + 签名头（与 AT-06 converter 协同）
  - 业务代码零改动

### AT-22: SSE 多实例 Redis pub/sub 广播

- **所属 ADR**：ADR-018
- **依赖**：AT-20
- **需求细节**：用户连接落任意实例，推送监听器发 Redis pub/sub，各实例订阅并推本地连接。
- **关键机制**：
  1. 推送监听器发布到 `notifications:{tenantId}:{userId}` channel。
  2. 各实例订阅，收到后查本地连接表推送。
  3. `@ConditionalOnProperty`：单体单实例不启用，集群启用。
- **涉及模块**：`xcms-message`/`xcms-portal`（Redis pub/sub）
- **验收标准**：多实例下用户收到推送（无论连哪个实例）。
- **实现模式（单体预留，可单体可拆分）**：
  - `PubSubPort` SPI（publish/subscribe）+ `NoOpPubSubPort`（`@ConditionalOnProperty("xcms.push.broadcast.enabled", matchIfMissing=true)`）——单体不广播，`NotificationSsePushListener` 直接 `registry.send`（AT-21 现状不变）
  - `RedisPubSubPort`（`@ConditionalOnProperty("xcms.push.broadcast.enabled", true)`）——集群启用，Listener 发 `PubSubPort.publish(notifications:{tenant}:{user})`，各实例订阅推本地连接
  - Redis 依赖 `@ConditionalOnProperty`，单体不引入
  - 业务监听器零改动（广播与否由 registry/PubSubPort 内部决定）

### AT-28: 异步任务多实例就绪（暂缓）

- **所属 ADR**：ADR-016
- **依赖**：无
- **需求细节**：运行态落 `task_runs` 表 + leader 接管。**优先级最低，暂缓实施**，待真正拆分/集群再启动。
- **关键机制**：见 ADR-016。
- **验收标准**：见 ADR-016。
- **实现模式（单体预留，暂缓）**：
  - `task_runs` 表 DDL 预留 `db/ddl/`（运行态落 DB，单体也可写增可观测性）
  - `TaskLockService` leader 锁已 DB 化（单实例无竞争，多实例自动接管）
  - 优先级最低，待真正集群再启动实现；单体单实例现状够用

### AT-29: web 基础设施抽取为独立 starter  ✅ 未开始

- **所属**：架构优化（横切依赖治理，为拆分复用铺路）
- **依赖**：AT-06（Security）、AT-08（@PreAuthorize）、AT-11（服务令牌）、AT-12（refresh cookie）——均已在 master
- **需求细节**：`spring-security-core` 现在 tenant/identity/org/auth-impl 4 个模块重复依赖；`SecurityConfig`/`GlobalExceptionHandler`/`JwtPermissionAuthenticationConverter`/`RestSecurityHandlers` 4 个配置类在 `xcms-app`，拆分后每个微服务要重写。抽取为独立 `xcms-web-starter` 模块，消除重复 + 配置可复用。
- **关键机制**：
  1. 新建 `xcms-web-starter` 模块，依赖 `xcms-shared-kernel` + `spring-boot-starter-web` + `spring-boot-starter-security` + `springdoc-openapi-starter-webmvc-ui`
  2. 4 个配置类从 `xcms-app/security/` 迁入 starter（作 `@AutoConfiguration`，`META-INF/spring/...AutoConfiguration.imports` 注册）
  3. **放行路径配置化**：`xcms.security.permit-paths`（默认 login/refresh/tenant-lookup/sso/actuator/swagger），各服务 `application.yml` 可覆盖
  4. **JwtDecoder 密钥配置项**：`xcms.jwt.secret`（各服务同源），starter 默认读
  5. **kernel pom 加 `spring-security-core`**（compile，提供 `@PreAuthorize` 注解编译期）——impl 依赖 kernel 即得注解，4 impl 删重复依赖
  6. **`xcms-app` 瘦身**：删 4 配置类，改依赖 `xcms-web-starter`；app 仅保留启动类 + profile 特定配置
  7. OpenAPI 配置迁 starter，`springdoc.info` 用配置项（title/version 默认取 `${spring.application.name}`）
- **涉及模块**：新建 `xcms-web-starter`、`xcms-shared-kernel` pom、4 个 impl pom、`xcms-app` 瘦身
- **验收标准**：4 impl 无重复 `spring-security-core`（传递自 kernel）；app 删 4 配置类；任意服务依赖 `xcms-web-starter` 即获得 web+security+doc 全套能力；单体编译通过；@PreAuthorize/401/403 行为不变。
- **实现性质**：单体即可做（消除重复依赖 + app 瘦身），拆分时各微服务依赖 starter 即就绪（零配置复用）。非阻塞当前迭代，但建议在拆分前完成。
- **设计依据**：Spring Boot starter 标准模式。kernel 管领域（实体/上下文/事件），starter 管 web 基础设施（过滤链/异常/OpenAPI），职责分离。kernel 已有 hibernate/jpa/springdoc-common（技术基础设施），但 `SecurityFilterChain` 是 servlet web 运行时，放 kernel 语义别扭，独立 starter 更清晰。

---

## 六、开发计划

| 迭代 | 周期 | 任务 | 里程碑 |
|---|---|---|---|
| **迭代一** | 第 1-2 周 | AT-01,02,03,06,07,15 | 事件消费端抽象落地、Security 接管鉴权、密码安全；单体可跑 |
| **迭代二** | 第 3-4 周 | AT-04,08,09,11,12,13,14 | ActorContext、方法级权限、服务令牌、refresh cookie、租户初始化时序修复 |
| **迭代三** | 第 5-7 周 | AT-16,17,18,19,20,21,23,24,25,26,27 | 数据权限下沉、SSE 推送、Prometheus 栈；单体形态完善 |
| **拆分前** | 按需 | AT-05,10,22,28,29 | Outbox、出站传播、Redis 广播、任务多实例、web-starter 抽取；微服务就绪 |

### 任务依赖速查

- **可立即并行启动**：AT-01（事件底座）、AT-06（Security）、AT-15（密码）、AT-20（SSE）、AT-24（Micrometer）
- **AT-01 阻塞**：AT-02,03,13,21
- **AT-06 阻塞**：AT-07,08,09,11,12
- **AT-09 阻塞**：AT-04,10,16
- **AT-16 阻塞**：AT-17,18
- **AT-17 阻塞**：AT-19
- **AT-20 阻塞**：AT-21,23

### 全局验收（迭代三末）

1. 事件发布/消费对称，监听器无手动租户切换，`eventId` 幂等
2. Spring Security 接管鉴权，方法级 `@PreAuthorize` 生效，401/403 标准 `ApiResponse`
3. `ActorContext` 替代 `TenantContext`，系统触发场景 principal 非 null
4. refresh token httpOnly cookie，不在 localStorage
5. 租户初始化不串租户，失败可补偿
6. 数据权限下沉 data-permission 模块，业务服务不依赖 auth-impl；脱敏 fail-closed
7. 前端 data-scope 对接 data-rule，无臆造接口
8. SSE 推送在线实时触达
9. Prometheus 指标暴露，`metric_value` 不再落库

---

> 本任务书为开发基准，开发人员按迭代与依赖执行。每个任务完成后对照验收标准自测，提 PR 评审。`docs/reviews/` 下的复盘/设计记录仅作参考，不作为基准。
