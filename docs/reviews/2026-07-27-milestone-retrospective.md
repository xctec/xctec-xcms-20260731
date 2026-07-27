# xcms 阶段复盘 / 里程碑评审（Milestone Retrospective）— MVP 实现对齐与缺口分析

> 文档定位：阶段复盘 / 里程碑评审（Milestone Retrospective）
> 复盘节点：MVP 实现对齐与缺口分析
> 角色视角：产品经理 / 设计师 / 架构师
> 梳理时间：2026-07-27
> 范围：后端 `xcms-backend`（以 `xcms-app` / `xcms-portal` / `xcms-identity` / `xcms-shared-kernel` / `xcms-authorization` 为主）
> 说明：问题按严重程度（Critical / High / Medium / Low）分类，均附文件定位与改进建议。

---

## Critical（安全 / 数据，需优先处理）

### C1. JWT 默认密钥硬编码且可预测 —— 可伪造任意 token（跨租户/越权）
- 位置：`xcms-app/src/main/resources/application.yml:51`、`JwtTokenProvider.java:27`
- 现象：
  ```yaml
  secret: ${XCMS_JWT_SECRET:xcms-jwt-default-secret-key-2026-07-25!}
  ```
  若生产环境未设置环境变量 `XCMS_JWT_SECRET`，将直接使用明文写死的默认值。该值已随代码公开，**任何人都可拿它签发合法 JWT**，进而冒充任意 `userId/tenantId`。
- 建议：
  1. 启动时校验密钥：未显式配置（或仍等于默认值）则**直接启动失败**，禁止带默认密钥上线。
  2. 密钥长度需 ≥ 256 bit（HS256 要求），建议在配置加载处做强度校验。
  3. 密钥通过配置中心 / KMS 注入，禁止提交到仓库或文档示例。

### C2. CORS 放行 `*` + 允许凭证 —— 任意站点可带凭证访问认证接口
- 位置：`application.yml:47`、`xcms-app/.../web/CorsConfig.java`
- 现象：`allowed-origins: "*"`，且 `CorsConfig` 中 `allowCredentials(true)` + `allowedOriginPatterns("*")`。对多租户认证系统而言，等价于允许任意第三方站点在用户浏览器中携带其凭证发起请求（如拿用户的 `Authorization` 头做跨站调用）。
- 建议：生产环境将 `allowed-origins` 收敛为具体可信前端域名白名单；对内网/管理后台进一步收紧。

---

## High（正确性 / 契约一致性）

### H1. 缺少全局异常处理（`@RestControllerAdvice`）—— 异常响应破坏 ApiResponse 契约
- 位置：全局仅 `xcms-authorization/.../advice/ColumnMaskResponseBodyAdvice.java`（用于脱敏，非异常处理）；搜索 `@RestControllerAdvice/@ControllerAdvice/@ExceptionHandler` 仅命中该脱敏类。
- 现象：`BusinessException` / `PermissionException` 等抛出后，未被统一转换为 `ApiResponse{errorCode,errorMsg,data}`，会落到 Spring 默认错误页（whitelabel），前端按 `errorCode` 解析会失败。
- 建议：新增 `GlobalExceptionHandler`（`@RestControllerAdvice`），将 `BusinessException/PermissionException/MethodArgumentNotValidException/未捕获异常` 统一包装为 `ApiResponse`，并映射到正确 HTTP 状态码（401/403/422/500 等）。

### H2. `TenantInterceptor` 的 401 响应与 `ApiResponse` 字段命名不一致
- 位置：`xcms-portal/.../web/TenantInterceptor.java`（`writeUnauthorized`）
- 现象：拦截器手写
  ```java
  response.getWriter().write("{\"code\":401,\"message\":\"" + msg + "\"}");
  ```
  而项目统一响应契约 `ApiResponse`（`xcms-shared-kernel/.../common/ApiResponse.java`）字段为 **`errorCode` / `errorMsg` / `data`**。键名（`code` vs `errorCode`、`message` vs `errorMsg`）和缺失 `data` 均与标准信封不一致，前端统一错误解析（通常读取 `errorCode`）会误判或漏判。
- 建议：401/403 统一委托给 `GlobalExceptionHandler` 或复用 `ApiResponse` 序列化，禁止在拦截器里手写 JSON；拦截器只负责 `TenantContext` 设置与"是否放行"判定。

---

## Medium（设计取舍 / 可维护性）

### M1. 未采用 Spring Security，自研鉴权链路 —— 缺少成熟生态兜底
- 位置：后端无 `SecurityFilterChain` / `@EnableWebSecurity` / `@EnableMethodSecurity`；仅 `pom` 引入 `spring-security-crypto`（`PasswordEncoder`）。请求鉴权由 `TenantInterceptor`（HandlerInterceptor）+ `JwtTokenProvider` + `JwtTenantResolver` 承担。
- 现象：可行但需团队自制方法级权限、`@PreAuthorize`、CSRF、OAuth2 Resource Server 等能力，长期维护成本与安全隐患较高。
- 建议：评估迁移到 Spring Security Resource Server（JWT）+ 方法级 `@PreAuthorize`，把"鉴权"与"租户解析"职责分离；至少明确把授权判断收敛到一处（后端强制校验，而非仅依赖前端 `<Can>`）。

### M2. 无状态 token 不可即时吊销（已知取舍）
- 位置：`xcms-identity/.../api/tenant/TenantResolver.java`（javadoc 已声明）；`JwtTokenProvider` 仅校验签名与 `exp`。
- 现象：`access-ttl-seconds: 7200`、`refresh-ttl-seconds: 604800`（7 天）。logout 仅标记会话 `EXPIRED`，token 在 `exp` 前仍有效；refresh token 7 天有效期较长，泄露后风险窗口大。
- 建议：引入短期 access + refresh 轮转，或加 token 黑名单 / 版本号（jti 校验）以支持即时吊销；对后台/管理操作使用更短 TTL。

### M3. 短密钥触发 `WeakKeyException` 且未优雅处理
- 位置：`JwtTokenProvider.java:30` `Keys.hmacShaKeyFor(secret.getBytes(...))`
- 现象：密钥 < 256 bit 时 JJWT 直接抛 `WeakKeyException`，构造期即失败，错误信息不友好。
- 建议：加载密钥时校验长度，不足则给出明确启动错误（与 C1 联动）。

### M4. 文档与实现漂移：`TenantContext` 签名
- 位置：`docs/02-architecture.md:329-340` 示例为 `set(Long tenantId, String dataSourceKey)` / `getDataSourceKey()`；实际 `xcms-shared-kernel/.../context/TenantContext.java` 为 `set(Long tenantId, Long userId, String dataSourceKey)` 并新增 `getCurrentUserId()`、`switchTo/restore`。
- 建议：同步文档，避免新成员按旧签名误用。

---

## Low（整洁度 / 质量 / 一致性）

### L1. 登录方法存在重复注释块
- 位置：`AuthServiceImpl.java:81-84`（`login()` 内两段几乎相同的注释重复粘贴）。
- 建议：清理冗余注释，保留一份即可。

### L2. 多租户隔离强依赖拦截器链，异步/内部线程存在空租户风险
- 位置：`@TenantId` 隔离依赖 `TenantContext`（`xcms-portal/.../web/TenantInterceptor` 设置）；`@Async`、调度任务、内部消息消费线程不经过该拦截器时 `TenantContext` 为空，`@TenantId` 取 null/默认值，可能串租户或越权。
- 建议：对后台任务/消费者显式设置租户；对"租户为空却访问租户表"的查询做兜底拦截或统一包装器。

### L3. 缺少安全相关限流与告警
- 位置：`AuthServiceImpl` 已实现登录失败锁定（`MAX_LOGIN_FAIL_ATTEMPTS=5`、`LOGIN_LOCK_MINUTES=30`，见 `:56-57`、`:103`、`:108`），但仅限账号级。
- 建议：补充 IP/账号级速率限制（如网关或 bucket4j），并对连续失败/异常登录做审计告警。

### L4. 核心安全链路自动化测试不足
- 位置：JWT 解析、租户隔离、权限判断等核心路径未见系统化测试。
- 建议：补充单测/集成测试，覆盖跨租户越权、token 篡改、过期、密钥强度等场景，降低重构风险。

### L5. 前端 types 与后端 DTO 手工对齐，存在漂移风险
- 位置：`docs/frontend/03-frontend-roadmap.md`「G8 后端类型对齐手工」已指出；后端已有 springdoc OpenAPI。
- 建议：引入 OpenAPI → TypeScript 类型生成，消除前后端契约漂移。

---

## 补充发现：租户创建数据初始化机制与风险（2026-07-27 续）

> 经对 `TenantServiceImpl.createTenant` / `TenantUserInitializer` / `TenantOrgInitializer` / `SeedDataService` / `InProcessEventPublisher` 交叉核对补充。
> 结论：项目**已实现**"共享库行级隔离 + `TenantCreatedEvent` 事件驱动多监听器"的租户数据初始化（此前未覆盖）；但存在 `@TenantId` 会话时序导致的串租户风险，需优先修复。

### S1（High）. 租户初始化监听器 `@TenantId` 时序 —— 可能串租户
- 位置：`TenantUserInitializer.java:30-75`、`TenantOrgInitializer.java:23-46`
- 现象：监听器用 `@EventListener @Transactional`（默认 `REQUIRED`），被 `InProcessEventPublisher` 同步在 `createTenant` 事务内触发，复用外层已开启的 Hibernate 会话。而 `@TenantId` 的当前租户由会话**开启时**捕获（见 `SeedDataService` 注释），`TenantContext.switchTo(tenantId)` 在会话开启后设置不生效，新用户/角色/部门的 `tenant_id` 可能写成**创建者租户**而非新租户。
- 建议：监听器事务改为 `Propagation.REQUIRES_NEW`；或参照 `SeedDataRunner`「调用方非事务、被调方法独立事务」的编排，确保初始化在捕获到新租户的新会话中执行。

### S2（Medium）. 幂等判断隐含依赖 `@TenantId` 自动过滤
- 位置：`TenantUserInitializer.java:56`、`TenantOrgInitializer.java:29`
- 现象：`findByUsername("admin").isEmpty()`、根部门 `findByParentIdIsNull...` 未带租户参数，正确性完全依赖实体 `@TenantId` 自动加过滤；若实体未加 `@TenantId` 或唯一约束为全局，则第二个租户会跳过初始化。
- 建议：显式按 `(tenantId, ...)` 查询，降低隐式约束风险。

### S3（Medium）. 新租户初始密码硬编码且日志明文
- 位置：`TenantUserInitializer.java:59`、日志 `:70`
- 现象：每个新租户 `admin` 初始密码固定 `admin123`，日志明文打印。
- 建议：随机生成初始密码 + 首登强制改密；日志不打印明文。

### S4（Low）. 初始化未解耦（非 AFTER_COMMIT）
- 位置：两监听器均用 `@EventListener`（非 `@TransactionalEventListener(AFTER_COMMIT)`）
- 现象：初始化失败会回滚整个 `createTenant`，租户创建与初始化强耦合。
- 建议：改用 `AFTER_COMMIT`，初始化失败可单独补偿重试。

### S5（Low）. 租户种子数据偏薄
- 仅角色/管理员/根部门；缺默认菜单、权限分配、岗位、数据权限规则等。
- 建议：按产品定义补充租户级种子数据清单。

### S6（High）. 审计"有查询无写入"——`@AuditLog` 埋点为 0，且落库丢弃 tenantId
- 位置：`AuditController.java`（仅 `/admin/audit/query` 查询）、`AuditAspect.java:35`（`@Around("@annotation(auditLog)")`）、`AuditEventListener.java:27-41`、`AuditLog.java`、`AuditServiceImpl.java:29-58`
- 现象：
  1. 写入链路框架已齐（`AuditAspect` 拦截 `@AuditLog` → 发布 `AuditEvent` → `AuditEventListener` 落库 `audit_log`），但全仓搜索 `@AuditLog` **实际应用点为 0**——没有任何 Controller/Service 方法标注该注解，切面永不触发，`AuditEvent` 也无人发布。审计日志表恒为空，仅有查询接口（查空表）。即"有查询、无写入"。
  2. `AuditEventListener.onAuditEvent` 落库时**未 `log.setTenantId(event.getTenantId())`**，`AuditEvent` 携带的 `tenantId` 被丢弃；`AuditLog extends TenantEntity`（含 `@TenantId`），tenant_id 实际靠当前线程 `TenantContext` 隐式填充，在异步/事件线程或 `TenantContext` 被清除时会写成空/错租户，破坏多租户隔离。
  3. `AuditServiceImpl.query` 的 `Specification` **未显式注入当前租户**，依赖 `@TenantId` 隐式过滤；同时不支持平台管理员跨租户查看全部审计。
  4. `AuditAspect.resolveOperatorId()` 优先取 `TenantContext.getCurrentUserId()`、回退反射 `SecurityContextHolder`；但项目自研鉴权未用 Spring Security，回退拿不到，操作者可能记为 null。
- 建议：
  1. 在关键写操作（登录/登出、用户/角色/权限变更、租户变更、文件/消息操作）批量补 `@AuditLog(module=..., type=...)` 埋点（可与菜单规范统一口径）。
  2. 监听器落库显式 `log.setTenantId(event.getTenantId())`（或 `TenantContext.set` 后 save），不依赖隐式；查询显式按当前租户过滤，并视需提供平台级跨租户视图。
  3. 操作者从 JWT 解析的当前用户取，去掉对 Spring Security 的反射回退。

### S7（High）. 异步事件转发机制未落地 + 无可靠投递保障
- 位置：`MqEventPublisher.java`、`EventPublisherAutoConfiguration.java:18-22`、`InProcessEventPublisher.java:29-33`、各 `*EventListener`
- 现象：
  1. **异步/MQ 转发只是骨架**：`MqEventPublisher` 用 `taskExecutor` 异步发送，但 `serializer`/`sender` 需外部注入，**无任何 `@Bean` 装配它**；`EventPublisherAutoConfiguration` 仅注册 `InProcessEventPublisher`（带 `@ConditionalOnMissingBean`）。全仓无 Kafka/RabbitMQ 依赖、无 `@EnableAsync`、无 `@Retryable`、无 outbox/event_store 表。实际永远是同步进程内发布，所谓"微服务形态可换 MQ"目前不成立。
  2. **无事件持久化/Outbox**：同步发布下，监听器下游失败或应用崩溃即丢事件，无 at-least-once 保证；跨模块副作用（审计落库、消息告警、未来 WebSocket 推送 F9）不可靠。
  3. **publish-before-commit**：即便接上 `MqEventPublisher`，其 `publish` 在业务事务内同步调度 `taskExecutor.execute(send)`，事务回滚也会把消息发出去，须用事务性 Outbox（本地消息表 + 后台转发）纠正。
  4. **无重试 / 死信**：`AuditEventListener`、`TaskFailedEventListener` 均吞异常仅 log，瞬时失败即丢弃。
  5. **监听器对 `TenantContext` 同步耦合**：`PermissionCacheEvictor`（`AFTER_COMMIT`）与 `AuditEventListener`（`@EventListener`，非 AFTER_COMMIT）都**不 `switchTo(event.getTenantId())`**，依赖当前线程 `TenantContext`；一旦异步/跨线程，`@TenantId` 会捕获空/错租户（与 S6 同源）。仅 `TaskFailedEventListener`、租户初始化器显式切换租户。
  6. **消费端幂等缺失**：MQ at-least-once 必重复投递，但 `MessageService.send`/`auditLogRepository.save` 无幂等键去重（`AuditEvent` 有 `eventId` 但未按它去重）。
- 建议：
  1. 短期：审计/组织初始化监听器改 `@TransactionalEventListener(AFTER_COMMIT)`；可异步监听器加 `@Async`+`@EnableAsync`，统一 `TenantContext` 传播（事件自带 tenantId + `switchTo`，或 `TaskDecorator`）；消费端按 `eventId` 去重。
  2. 中期（微服务/跨服务前必做）：落地**事务性 Outbox**（事件落 `event_outbox` 随业务事务提交，后台线程/调度轮询转发 MQ），补重试 + 死信；`MqEventPublisher` 真正装配为 `@ConditionalOnProperty` 开关并配齐序列化/发送器。
  3. 长期：事件版本化（`DomainEvent` 增 `version()`），统一 `topic()` 命名规范。

### S8（High）. 微服务间调用的租户/用户上下文传播机制缺失
- 位置：`TenantInterceptor.java:40-54`（仅 `xcms-portal` 入口的 `HandlerInterceptor`）、`TenantContext`（ThreadLocal）、全仓无 `Feign`/`RestTemplate`/`WebClient` 出站拦截器
- 现象：
  1. **当前仅"入口解析"、无"出站传播"**：`tenantId/userId` 只在 `xcms-portal` 的 Web 入口由 `TenantInterceptor` 从 `Authorization: Bearer <token>` 解析并写入 `TenantContext`（ThreadLocal）。单体下所有模块同进程、同线程，下游 `@Autowired` 调用天然继承，**能跑**。但全仓无任何 `ClientHttpRequestInterceptor`（RestTemplate）/ `RequestInterceptor`（Feign）/ `ExchangeFilterFunction`（WebClient）把当前 `tenantId/userId` 或 token 带到**出站 HTTP 调用**。
  2. **拆微服务后首跳之后即断链**：服务 A→服务 B 的 HTTP 调用若不带凭证，B 的 `TenantInterceptor` 会因无 `Authorization` 直接 401；即便 A 转发用户 JWT，也面临 token 中途过期、且把"用户身份"强加给"系统调用"的信任混淆。
  3. **非 HTTP 触发源无 token**：定时任务（`TaskSchedulerEngine` 用 `switchTo(tenantId)`）、MQ 消费者（`TaskFailedEventListener` 用 `switchTo(event.getTenantId())`）、事件监听器都**没有 JWT**，靠事件/任务记录自带的 `tenantId` 手动设 `TenantContext`。一旦这些流程要跨服务调 HTTP，没有任何可转发的凭证。
  4. **ThreadLocal 不跨线程/跨进程**：与 S7 同源，异步线程（线程池/调度/`@Async`）、跨进程 HTTP 都会丢失 `TenantContext`，仅靠拦截器入口填充在微服务异步化后不可靠。
- 建议：
  1. 引入**出站上下文传播拦截器**：统一把 `tenantId/userId` 经签名内部头（如 `X-Tenant-Id`/`X-User-Id` + 内部 `X-Service-Token`）或转发 JWT，注入 Feign/RestTemplate/WebClient，使下游服务 `TenantInterceptor` 可解析（目标服务需同时支持"用户 JWT"与"内部服务令牌"两种解析）。
  2. 增加**系统/服务令牌（service account）**：为调度、MQ 消费者等无用户态的触发源签发服务级 JWT（含 tenantId + service principal），使其跨服务调用可被目标服务鉴权，而非复用/伪造用户 token。
  3. **边车/网关注入租户上下文**：在网关层统一校验并注入租户头，内部服务间信任签名头（配合 mTLS），减少逐跳解析 JWT 的重复成本与密钥暴露面。
  4. `TenantContext` 传播不再仅依赖 ThreadLocal：异步场景用 `TaskDecorator` 显式传递；跨服务以"事件自带 tenantId + 出站头"为准（与 S7 的 `@Async`/Outbox 改造协同）。
- 关联：与 S7（异步事件转发）同源，同属"微服务就绪度"缺口，应在 Phase 3 末/Phase 4 前一并补齐（见 `README.md` 建设周期）。

### S9（High）. 应明确为"无状态服务"设计 —— 当前存在 3 处实例本地状态泄漏
- 位置：`PermissionCacheConfig.java:11-21`（Caffeine 本地缓存）、`SsoServiceImpl.java:42-51`（Caffeine `ssoStateCache`）、`TaskSchedulerEngine.java:38`（内存 `futures` Map）、本地文件存储
- 现状（已是 stateless 的基础）：鉴权用 JWT（`TenantInterceptor` 解析 Bearer，全仓无 `HttpSession`）；`TenantContext` 为 ThreadLocal（请求级）；`TaskLockService` 为 DB 行锁 + leader 选举 → 这些已满足无状态前提。
- 问题（多实例水平扩容会出错的本地态）：
  1. **权限缓存是本地 Caffeine（`userPermissions`）**：`@CacheEvict` 只清**当前实例**缓存（`PermissionCacheEvictor` 用 `allEntries=true` 也仅本实例）。多实例下，实例 A 改权限后，实例 B 仍是旧缓存直到 TTL（5 min），且 `PermissionChangedEvent` 只在本实例触发 → **跨实例权限变更不一致**（安全正确性问题）。
  2. **SSO 的 OAuth `state` 存在本地 Caffeine（`ssoStateCache`）**：`authorize()` 命中实例 A 写入 state，`validateSsoState()`（IdP 回调）可能落到实例 B → state 不存在 → **SSO 登录间歇性失败**。这是典型的"服务端会话态"，直接违反无状态。
  3. **`TaskSchedulerEngine.futures = ConcurrentHashMap<Long, ScheduledFuture>` 为实例内存**：只领导者实例有运行态；leader 宕机后 in-flight 任务句柄丢失、无跨实例接管；运行态未落在 DB（`task_runs`），水平扩容无一致性保障。
  4. **本地文件存储**：各实例写各自磁盘，文件不共享（与 F8/OSS 缺口同源）。
- 建议（明确"无状态服务"为架构原则，并消除上述泄漏）：
  1. 发布 ADR：所有服务实例**可随时启停、任意扩缩、无请求亲和（非 sticky）**，任何实例级可变状态必须外置。
  2. 权限缓存 → 分布式缓存（Redis）；或改用"短 TTL + `PermissionChangedEvent` 经 MQ 广播失效（与 S7 Outbox 协同）"。
  3. SSO `state` → Redis 外置，**或改为自校验签名 token**（state 用 JWT/HMAC 签名、不落服务器内存，彻底无状态）。
  4. 任务运行态 → 落 DB（`task_runs` 记录执行中/完成），不依赖实例内存 `futures` 做跨实例可见性；leader 选举已 DB 化，配合健康探活实现故障接管。
  5. 文件 → 共享存储/对象存储（OSS），并预留未来 WebSocket/SSE（F9）需 Redis pub/sub 跨实例广播，从设计之初按无状态考量。
- 关联：是 S7/S8（微服务就绪）的**前置底座**——若实例有本地态，多实例部署与跨服务调用都不可靠。

### S10（Medium）. 事件"有 publish 抽象、无 listener 抽象"——消费端端口缺失，六边形不对称
- 位置：`DomainEventPublisher`（输出端口）、`DomainEvent`（含 `topic()`/`eventId()`/`tenantId()`/`occurredAt()`）、各 `*EventListener`（裸 `@Component` + `@EventListener`/`@TransactionalEventListener`）
- 现象：
  1. **发布端是干净的六边形端口**：`DomainEventPublisher` 接口 + `DomainEvent` 契约，可经 `@ConditionalOnMissingBean` 在 `InProcessEventPublisher` ↔ `MqEventPublisher` 间无缝替换，业务代码不感知。
  2. **消费端无抽象**：全仓无 `DomainEventListener<T>` / `EventHandler<E>` 之类的监听端口；`AuditEventListener`、`TaskFailedEventListener`、`PermissionCacheEvictor`、`TenantUserInitializer`、`TenantOrgInitializer` 全部是裸 `@Component` 直接吃 Spring `ApplicationEventPublisher` 的 `@EventListener`。即"入站事件端口"缺失，六边形在消费侧不对称。
  3. **无统一路由/分发契约**：没有"按 `topic()`/事件类型把 `DomainEvent` 派发到对应 handler"的注册表，也无法枚举"本服务消费哪些事件"；迁 MQ 时只能逐监听器手工接消费端。
  4. **租户绑定/重试/幂等被摊到每个监听器**（与 S7/S8 同源）：有的 `switchTo(event.getTenantId())`（TaskFailedEventListener、租户初始化器），有的不切（AuditEventListener、PermissionCacheEvictor）；重试/DLQ/`eventId` 去重也无统一落点。
- 影响：发布端可换、消费端不可换；S7（MQ 转发）、S8（跨服务租户传播）、S7 的重试/幂等无法"一处治理"，只能逐监听器改。
- 建议（补上入站事件端口，使六边形对称）：
  1. 定义监听端口：`public interface DomainEventListener<E extends DomainEvent> { void onEvent(E event); Class<E> eventType(); }`，各监听器实现它并 `@Component` 注册。
  2. 引入统一**分发器/注册表**：用一个 `DomainEventDispatcher` 收集所有 `DomainEventListener` Bean 按 `eventType()` 路由；进程内由单个 `@EventListener(DomainEvent.class)` 委托它，MQ 模式由单个消费者反序列化后同样委托它 → 发布端、消费端对称可换。
  3. 横切能力集中到分发器：在此统一做 `TenantContext.switchTo(event.getTenantId())`（消除 S7/S8 各监听器不一致）、`eventId` 幂等去重、失败重试/死信（S7）——业务监听器只写 `onEvent` 纯逻辑。
  4. 收益：可单测 handler（无需 Spring 事件总线）、可枚举订阅关系、异步/跨服务迁移成本骤降。
- 关联：与 S7/S8 强相关——本抽象是 S7（MQ+重试+幂等）与 S8（租户传播）的**统一承载点**，建议与 S7 同批落地。

### S11（High）. 工作流"引擎任务 → 自研 `wf_task` / 业务单据"回写机制缺失（拉取式、非事件驱动、不回写业务）
- 位置：`WorkflowServiceImpl.java:145`（`start`→`syncTasks`）、`WorkflowServiceImpl.java:166-195`（`complete`）、`WorkflowServiceImpl.java:227-248`（`syncTasks` 拉取式）、`WorkflowTask.java:22`（`flowTaskId` 关联）、`FlowableH2DatabaseTypeConfig.java`（Flowable 8 引擎装配）
- 现状（已落地的机制）：Flowable 是**真实引擎**（自管 `ACT_RU_TASK`/`ACT_RU_EXECUTION`/`ACT_HI_*`），自研 `wf_task`/`wf_instance`/`wf_definition` 是**业务侧镜像表**，靠 `flowTaskId`/`flowInstanceId` 关联引擎。回写是**"拉取式同步"**：仅在 `start()`、`complete()` 业务节点调 `syncTasks()`，把 `taskService.createTaskQuery().processInstanceId(...)` 的引擎当前任务**拉进** `wf_task`；`complete()` 再用 `wf_task.flowTaskId` 调 `taskService.complete()` 并回写 `wf_task`/`wf_instance` 状态。`businessKey` 仅存入 `wf_instance`，不回写业务单据。
- 问题：
  1. **回写是拉取式、非事件驱动**：完全依赖 `start/complete` 时机主动 `syncTasks()`；Flowable 因定时器/边界事件/信号/消息事件、并行网关/多实例分支、ServiceTask 自动完成、或外部 API 直接操作引擎而产生的任务变化，**不会触发** `wf_task` 同步 → `wf_task` 与 `ACT_RU_TASK` 状态漂移。全模块无 `FlowableEventListener` 监听 `TASK_CREATED`/`TASK_COMPLETED`/`PROCESS_COMPLETED` 来驱动回写。
  2. **async executor 竞态**：若启用 Flowable 异步节点，`taskService.complete()` 返回时下一节点任务可能尚未创建，`syncTasks()` 同步立即查询会**漏同步**；代码在事务内立即查，未等 job 提交后存在。
  3. **取消/删除无回写**：`syncTasks` 只"新增不存在的"（`findByFlowTaskId().isPresent()` 则跳过），引擎任务被删/取消（`ACT_RU_TASK` 行消失）时 `wf_task` 不会同步置 `CANCELLED`/删除 → 残留**幽灵待办**。
  4. **"回写用户数据"（业务单据）缺失**：`complete()` 只更新 `wf_task`/`wf_instance` 自身，**不回写 `businessKey` 对应的业务单据**（审批结果/同意拒绝/`comment`/`formData` 都没落回业务表）。流程跑完，业务侧单据状态无人更新——缺"流程完成 → 业务回调"机制。
  5. **权威源不清**：`wf_task` 既是镜像又承载 `comment`/`formData`/`claimTime` 等业务字段，Flowable 才是运行时权威源；二者无对账/补偿，长流程异常路径下易不一致。
- 建议（把"引擎 → 自研/业务"回写做成事件驱动 + 最终一致）：
  1. 引入 **Flowable 事件监听**（`FlowableEventListener` / Spring `EventRegistry`），监听 `TASK_CREATED`/`TASK_COMPLETED`/`PROCESS_COMPLETED`/`PROCESS_CANCELLED`，统一驱动 `wf_task`/`wf_instance` 的增删改——事件驱动回写替代/补充 `syncTasks` 拉取；与 S10 `DomainEventListener` 分发器对接：将 Flowable 事件**适配成 `DomainEvent`** 进入统一分发器。
  2. 流程完成（任务级/流程级）**发布领域事件** `WorkflowTaskCompletedEvent`/`WorkflowCompletedEvent`（携带 `businessKey` + `outcome` + `formData`），业务模块订阅后回写自身单据；复用 S7 的 Outbox 保证 at-least-once + 幂等（与 S10 的 `eventId` 去重协同），彻底解耦工作流与业务。
  3. `complete()` 后不再依赖同步 `syncTasks` 立即查，改为事件驱动 + 最终一致；或至少在事务 `AFTER_COMMIT` 后再 `syncTasks`（消除 async 竞态、避免漏同步）。
  4. 处理取消/删除：监听取消类事件将 `wf_task` 置 `CANCELLED`，消除幽灵待办。
  5. 定义权威源与对账：明确 Flowable 为运行时权威，`wf_task` 为读模型（CQRS 思路），必要时加定时对账补偿任务。
- 关联：与 S7（事件/Outbox）、S8（跨服务，若业务模块独立部署）、S10（`DomainEventListener` 分发器承载 Flowable 事件适配）强相关，建议与 S7/S10 同批落地。

### S12（Low）. `TenantContext` 名实不符：实为"调用者身份上下文"，且 `userId` 在系统触发场景为 null、`dataSourceKey` 为死字段
- 位置：`TenantContext.java:7-61`（`TenantInfo(tenantId, userId, dataSourceKey)`）、类注释"传递租户与当前用户信息"
- 现状：上下文用 `ThreadLocal<TenantInfo>` 存三字段：
  - `tenantId`：JPA `@TenantId` / `CurrentTenantIdentifierResolver` 读取，做行级多租户隔离；也是所有业务查询隐式 `WHERE tenant_id=?` 与按租户聚合的锚（`MetricServiceImpl`/`OperationLogServiceImpl`/`AlertRuleServiceImpl`）。
  - `userId`："当前操作人"，被 135+ 处使用：审计操作人（`OperationLogServiceImpl.setOperatorId`）、创建人（`TenantServiceImpl.setCreatedBy`）、消息发送人（`MessageServiceImpl.senderId`）、权限校验主体（`PermissionService.checkPermission(userId,...)`，工作流 `deploy/start/complete` 均查）、待办归属（`taskRepository.findByAssigneeId`）、工作流发起/办理人。即它本质是**调用者身份上下文**，而非纯租户上下文。
  - `dataSourceKey`：注释/字段暗示"每租户独立数据源"，但所有 `set` 默认传 `"shared"`，当前为共享库+行级隔离，该字段实际恒为 `shared`（占位/预留，无 `AbstractRoutingDataSource` 消费）。
- 问题：
  1. **命名误导（职责膨胀）**：叫 `TenantContext` 却承载 `userId`（乃至 `dataSourceKey`），本质应是 `ActorContext`/`RequestContext`/`AuthContext`。命名即文档，误导维护者以为只含租户，导致 S8 讨论"跨服务传播"时必须额外带 `X-User-Id`、`dataSourceKey` 也要考虑——传播面被悄悄放大。
  2. **`userId` 在系统触发场景为 null**：定时任务（`TaskSchedulerEngine` 用 `switchTo(tenantId)`，未带 userId）、MQ 消费者（`TaskFailedEventListener` 用 `switchTo(event.getTenantId())`）均无 JWT，`getCurrentUserId()` 返回 null。随后审计 `operatorId`、创建人 `createdBy`、消息 `senderId` 会落 null → 与 **S6（审计操作者取值）** 同源的"操作者丢失"问题，且无人校验/兜底。
  3. **`dataSourceKey` 死字段**：从未被读取做真实数据源路由（全仓无 `AbstractRoutingDataSource`/`determineCurrentLookupKey` 使用它），增加 ThreadLocal 体积与跨线程/跨服务传播成本却无收益。
- 建议：
  1. 更名为 **`ActorContext`/`RequestContext`/`AuthContext`**，明确"调用者身份"语义；或拆为 `TenantContext(tenantId)` + `PrincipalContext(userId, ...)` 两个职责清晰的上下文（与 S9 无状态、S8 传播协同，传播时各取所需）。
  2. `userId` 缺失时显式处理：系统触发源（调度/MQ）应注入**服务身份**（service principal，见 S8 的服务令牌），使审计 `operatorId` 记为 `system@<service>` 而非 null；或在 `getCurrentUserId()` 返回 null 处做明确兜底/校验（与 S6 合并治理）。
  3. 删除或真正落地 `dataSourceKey`：近期不做"一租户一库"则移除该字段以减少传播面；若做则接入 `AbstractRoutingDataSource` 让其为实际生效，而非恒 `shared`。
- 关联：与 S6（审计操作者）、S8（跨服务需携带 userId）、S9（ThreadLocal 持有 userId 使无状态传播变复杂）同源；重命名/拆分能让 S8 出站头设计更精准。

### S13（High）. `ColumnMaskResponseBodyAdvice` 是 HTTP 出口切面，微服务场景下"无效果/效果错误"
- 位置：`ColumnMaskResponseBodyAdvice.java:25`（`@ControllerAdvice`）、`:32-38`（`supports` 依赖 `@MaskResource`）、`:55-67`（`beforeBodyWrite` 仅处理 `ApiResponse`、catch 仅 log）、`DataPermissionServiceImpl.java:144-148`（`findByResourceTypeAndTenantId(resourceType, tenantId)`）、`:154,181-186`（`isUserAllowed`：`userId==null` → 全字段脱敏）、`UserController.java:64,71`（全项目**仅** `UserController` 的 `get/getByUsername` 标了 `@MaskResource`）
- 机制现状：`ResponseBodyAdvice` 是 **Spring MVC HTTP 出口切面**——只在「当前服务的 `@RestController` 方法/类标注 `@MaskResource` 且返回 `ApiResponse<?>`」时，于响应写出前调 `dataPermissionService.applyColumnMask(...)` 脱敏；脱敏规则按 `resourceType` + `TenantContext.getTenantId()` 查，是否脱某字段按 `TenantContext.getCurrentUserId()` 的角色匹配决定。
- 微服务场景下的失效/错误：
  1. **能力碎片化（未引入/未标注即不脱敏）**：该 `@ControllerAdvice` 仅注册在引入 `xcms-auth-impl` 的服务；拆分后**每个微服务需各自引入且各自给 Controller 标 `@MaskResource`**，否则完全不脱敏。当前全项目仅 `UserController` 两处标注——覆盖面本就极窄，微服务下缺口只会放大。
  2. **依赖 `TenantContext` 上下文 → 跨服务即错配（与 S8/S12 同源）**：
     - **tenantId 为 null（跨服务未传播租户）→ 规则查不到 → 裸奔**：`applyColumnMask` 用 `tenantId=null` 查 `findByResourceTypeAndTenantId` → 结果为空 → `masks.isEmpty()` 直接 `return entity`（**fail-open**），敏感数据**未脱敏返回**。
     - **userId 为 null（系统触发/内部调用无用户）→ 全字段脱敏**：`isUserAllowed` 在 `userId==null` 时 `return false` → 对所有字段脱敏（过度），破坏服务间数据完整性。
  3. **脱敏在出口分散执行，无法在"出网边界"统一保证**：微服务下数据常被聚合层/BFF 组合返回；若聚合 Controller 没标 `@MaskResource`，而下游服务 B 自身脱敏了则 OK、B 没脱敏则 A 透传原样 → **漏脱敏**；反之 B 对"服务间内部调用"也脱敏，会破坏 A 的业务逻辑。脱敏应是面向最终用户的**出网策略**，而非每服务 HTTP 出口各自为政。
  4. **`ApiResponse` 强耦合**：`beforeBodyWrite` 仅处理 `body instanceof ApiResponse`。若返回 `PageResult`/`List` 裸/自定义包装/`ResponseEntity`/流式响应，则**不脱敏**；微服务协议更多样，更易漏。
  5. **异常即裸奔**：`beforeBodyWrite` 的 `try/catch` 仅 `log.warn` 后返回原 body——脱敏链路（角色解析、反射）任一异常即**以未脱敏数据返回**。
  6. **`@Entity` 跳过脱敏**：`applyColumnMask` 遇 JPA 实体直接跳过并告警（防数据污染），但若有人把实体当 DTO 返回则**不脱敏**（边角正确性问题）。
- 建议（脱敏下沉为"数据出口边界策略"，与 S8 协同）：
  1. **解耦 HTTP 出口切面**：脱敏不应依赖"每 Controller 标 `@MaskResource` + `ResponseBodyAdvice`"。下沉到**DTO 映射层/序列化层（toDTO 后立即脱敏）** 或 **网关/BFF 出网边界统一执行**，与"谁返回"解耦。
  2. **fail-closed**：`applyColumnMask` 在 `tenantId==null` 或缺规则时应**默认脱敏/拒绝**，而非 `return entity` 裸奔（高危 fail-open）。
  3. **跨服务必须携带租户+用户+角色上下文**（S8 出站拦截器/服务令牌）；内部服务调用以 service token 标识"系统调用"，按"出网才脱敏、内部不脱敏"区分语义，避免破坏服务间数据完整性。
  4. 解除 `ApiResponse` 耦合，脱敏在更底层（序列化/DTO）进行；明确"仅面向最终用户出网脱敏"的策略边界。
- 关联：与 S8（跨服务上下文传播）、S12（`userId`/`tenantId` 为 null 导致错配）、S9（无状态）、安全基线（F2）强相关；是"微服务就绪"前必须修正的安全控制失效点。

### S14（Info，约定沉淀，非改动项）. OSIV 已正确关闭——多租户 `@TenantId` 会话冻结是关它的关键动因，应保持并守住关后边界
- 位置：`application.yml:8-11`（`open-in-view: false`，注释："避免请求入口提前开 Session 导致 `@TenantId` 会话租户冻结为默认 0，使登录等需在事务内设置租户的场景查不到数据"）
- 现状：**项目已显式关闭 OSIV**（Spring Boot 默认开启）。关闭动因是**多租户而非通用性能**：OSIV 在请求最早期（Controller 之前、视图渲染前的整个请求）即开启 Hibernate Session，而 `@TenantId` 的租户解析在 Session 打开时即冻结；若 Session 早于 `TenantInterceptor`（设 TenantContext）开启，`@TenantId` 取到默认租户 0，使"在事务内 `switchTo(tenantId)`"的场景（登录、租户初始化，见 S1）查不到数据。关闭后 Session 仅在 `@Transactional` 进入时打开，此时 TenantContext 已就绪，租户解析正确。
- 关后正确性核查（当前安全）：
  - Controller 不直接返回 JPA 实体（搜 `return repository.find*` 在 Controller 层为 0 处），响应均为 `ApiResponse<DTO>`，序列化阶段（`ColumnMaskResponseBodyAdvice`）只访问 DTO 字段、不触碰 lazy 实体关联 → 不会 `LazyInitializationException`。
  - `@Transactional` 覆盖普遍（35+ 处 service 层），DB 访问多在事务窗口内。
- 关后必须守住的边界（防回归 / 防有人图省事重开 OSIV）：
  1. **DB 访问一律在 `@Transactional` 内**：禁止在 Controller / 工具方法 / 非事务层裸调 repository 并访问 lazy 关联。
  2. **DTO 映射在事务内完成**：service 返回前把 lazy 关联映射进 DTO，不把含未初始化 lazy 的实体传给序列化层（OSIV 关闭后，序列化期访问即抛 `LazyInitializationException`）。
  3. **事件/异步监听器访问 lazy 需先 fetch**：`@TransactionalEventListener(AFTER_COMMIT)`、MQ 消费者、`@Async` 任务默认不在原 Session 内，访问 lazy 前须在事务内用 `JOIN FETCH`/`EntityGraph` 取好（与 S7/S10 协同）。
  4. **连接池收益**：请求线程仅在事务窗口持有 DB 连接，利于 S9 无状态 + 水平扩容的连接效率；不要在事务外做无关阻塞以延长连接占用。
- 结论：**应保持关闭**，这是本项目相对 Spring Boot 默认值的正确偏离。无需改代码，仅需把"关后边界"作为约定沉淀并纳入 Code Review 检查项。
- 关联：与 S1（`@TenantId` 会话租户冻结正是关 OSIV 的动因）、S9（无状态/连接池）、S7/S10（事件/异步监听器的 lazy 边界）相关。

### S15（High，建议采用）. 指标采集/告警应改用 Prometheus + Grafana，自建 `metric_value` 落库 + 玩具级告警引擎不具扩展性
- 位置：`MetricServiceImpl.java:53-101`（`collect()` 用 `ManagementFactory` 取 JVM 指标 + 遍历 `MetricProvider`，逐条 `metricValueRepository.save` 落 `metric_value`）、`AlertRuleServiceImpl.java:135-165`（`evaluate()` 仅取 `getLatest` 瞬时值比对，规则字段 `durationMin` **完全未被使用**）、`xcms-app/pom.xml:81`（仅 `spring-boot-starter-actuator`，无 `micrometer-registry-prometheus`）
- 现状：指标采集是 **push 式自建设施**——`collect()` 从 `ManagementFactory` 取 JVM 指标、遍历 `MetricProvider` SPI 取业务指标，每条样本 `save` 进关系库 `metric_value`；告警是 **自建引擎**——`evaluate()` 遍历启用规则、调 `getLatest(metricKey)` 取最新一条、用 `match()` 做 `>/</>=/<=` 瞬时比较，触发则写 `alert_record` + `MessageService.send`。已引入 actuator，但未接 micrometer-prometheus。
- 问题（为何不如 Prometheus + Grafana）：
  1. **时序数据存关系库**：每样本 `save` 进 `metric_value`，高基数（租户×指标×采集周期）下疯狂写 OLTP 库、无压缩、查询慢；把本不该由业务库承担的时间序列负载压给了 `xcms-operation`。
  2. **无实例维度 + 多实例错乱（与 S9 相关）**：`collect()` 每实例都写自己的指标，`metric_value` 无 `instance` 标签，多实例下无法区分/聚合；Prometheus pull 模式天然按 `instance`/`job` 打标、service discovery 自动发现，与无状态多实例（S9）契合。
  3. **JVM 指标手写且落后**：堆内存/线程靠 `ManagementFactory` 手动取并落库，而 actuator + micrometer 可**自动暴露**实时值，Prometheus 抓取的是实时序列而非历史快照、Grafana 直接画；`PermissionCacheConfig` 注释已提"配合 actuator/metrics 观察"，却未落地到 Prometheus。
  4. **告警引擎是玩具级（含真实缺陷）**：`evaluate()` 只看**瞬时最新值**，规则 `durationMin` 字段形同虚设——**无"持续 N 分钟"语义**；不支持 PromQL 表达式、多条件、同比/环比、no-data 检测、抑制/静默/路由分组；`AlertRecord` 只记 `OPEN`、无 `resolved` 自动流转、无告警状态机。
  5. **前端看板缺计量能力（与 F6 同源）**：`querySeries` 返 DB 历史，但缺 Grafana 级图表/仪表盘；F6 指出的"运营看板静态 mock、缺计量"恰可被 Grafana 直接补齐。
- 建议（迁移到业界标准可观测性栈）：
  1. 接入 **Micrometer + Prometheus**：加 `micrometer-registry-prometheus` 暴露 `/actuator/prometheus`；将 `MetricProvider.collectMetrics` 语义从"落 DB"改为"向 Micrometer 注册 `Meter`（Counter/Gauge/Timer）"，业务指标自动进入 Prometheus；JVM/缓存指标由 actuator 自动暴露，移除 `metric_value` 落库。
  2. **可视化用 Grafana**：接 Prometheus 数据源替代 F6 的静态 mock 看板；多租户看板以 `tenant` label 过滤。
  3. **告警迁 Alertmanager**：用 PromQL + `for: 5m` 持续时间 + 分组/抑制/静默 + 多通道（邮件/钉钉/Webhook）；保留 `AlertRule` 作规则镜像或改为消费 Alertmanager webhook 写 `alert_record` 归档，而非自算阈值——`durationMin` 终于落到实处。
  4. **多租户基数控制**：`tenant` 作 label 需谨慎（租户数×指标×实例易基数爆炸）；可仅对关键租户打 label，或按租户做 Prometheus 远程写分片，避免全量标签化压垮 TSDB。
  5. 保留 `MetricProvider` SPI 作为"业务指标注册点"（良好扩展设计），`collect()` 定时任务可作自定义聚合兜底，但主流指标走 Prometheus pull。
- 关联：解决 F6（运营看板 mock/缺计量）；与 S9（无状态多实例指标采集契合）、S8（微服务实例发现）协同；是"可观测性"这一微服务就绪能力的核心组成。

---

## 优先级建议（下一步）

1. **立即**：C1（JWT 密钥启动强校验）、C2（CORS 收敛）。
2. **本周**：H1 + H2（统一 `GlobalExceptionHandler` + `ApiResponse` 错误契约）。
3. **迭代内**：M2（token 吊销）、M3（密钥强度）、L2（异步租户兜底）、L4（测试）。
4. **规划**：M1（是否迁移 Spring Security）、L5（OpenAPI 生成类型）。
5. **本周（补充）**：S1（租户初始化 `@TenantId` 串租户修复）、S3（初始密码随机化 + 首登改密）。
6. **迭代内**：S6（审计 `@AuditLog` 埋点补齐 + 监听器 tenantId 落库 + 操作者取值修正）。
7. **迭代内（基础设施）**：S7（事件转发补齐 —— `@Async` + `TenantContext` 传播 + 消费幂等；中期落地事务性 Outbox + MQ 装配 + 重试/死信）。
8. **微服务就绪（Phase 3 末/Phase 4 前）**：S8（租户/用户上下文跨服务传播 —— 出站拦截器 + 服务令牌 + 网关注入租户头）。
9. **架构原则（立即可定）**：S9（明确"无状态服务"设计 —— 消除本地 Caffeine 权限/SSO 缓存与内存 `futures` 等实例态，外置到 Redis/DB/OSS）。
10. **与 S7 同批（基础设施）**：S10（补入站事件端口 `DomainEventListener<E>` + 统一分发器，使发布/消费对称，集中承载租户传播/幂等/重试）。
11. **与 S7/S10 同批（基础设施）**：S11（工作流回写事件驱动化 —— 引入 Flowable 事件监听适配为 `DomainEvent` 驱动 `wf_task` 回写，流程完成发 `WorkflowCompletedEvent(businessKey, outcome)` 回写业务单据，消除拉取式漂移/幽灵待办/不回写业务）。
12. **整洁度（与 S6/S8/S9 协同）**：S12（`TenantContext` 更名 `ActorContext`/拆分，系统触发注入 service principal 替代 null `userId`，删除死字段 `dataSourceKey`）。
13. **安全/微服务就绪（High）**：S13（列脱敏从 `ResponseBodyAdvice` 下沉为出网边界策略 + fail-closed，跨服务必须携带租户/用户/角色上下文，解除 `ApiResponse` 耦合）。
14. **约定沉淀（Info，非改动）**：S14（OSIV 保持关闭——多租户 `@TenantId` 会话冻结是动因；守 `DTO 事务内映射`/`监听器事务内 fetch lazy` 边界，纳入 CR 检查项）。
15. **可观测性（High，建议采用）**：S15（指标采集/告警迁 Prometheus+Grafana+Alertmanager，接入 Micrometer 去 `metric_value` 落库；`MetricProvider` 改为注册 Meter；顺带解决 F6 看板）。

---

## 功能缺口分析（与文档/菜单对照，2026-07-27）

> 方法：以 `docs/06-menu-specification.md`、`docs/01-overview.md`、`docs/09-roadmap.md` 的"功能全集"为基准，逐模块核对后端 26 个 Controller + 前端页面实际实现。
> 结论：**后端接口面较完整（模块化 api/impl），但"壳已具、肉未填"——大量高级能力与前端集成缺失。**

### F1. 前端与后端大面积未打通（最突出）
- 仅 **5 个页面接入后端 API**：`Login`、`UserList`、`TenantList`、`Permission`、`Organization`（搜索 `useQuery/useMutation/@/api` 仅命中这 5 个）。
- 以下页面仍为**写死 mock（无数据、无 CRUD、无 API 调用）**：`Operation`（运营看板）、`Workflow`（流程管理）、`Message`、`Config`、`FileManagement`、`Audit`、`TaskSchedule`。
- 影响：后端 26 个控制器、数百接口已就绪，但管理端呈现"空壳"，核心模块（流程/消息/文件/审计/调度/运营）不可真正使用。

### F2. 身份与权限
- **缺 MFA / 二次验证**：全仓搜索 `Mfa|TOTP|2FA` 为 0 命中。仅"密码 + 失败锁定"，不满足中台安全基线（文档/安全规范通常要求）。
- **SSO 仅 OAuth2 / OIDC**：`AuthServiceImpl` 登录仅处理 `OAUTH2`/`OIDC` 协议分支；**SAML 及微信/钉钉/飞书原生协议未实现**（仅有 Provider 元数据管理，无对应回调/令牌交换）。
- **缺跨租户用户切换（代操作 / impersonate）**：搜索 `switchTenant|impersonate` 为 0 命中（Phase 3 内部能力未落地）。

### F3. 消息（仅站内信，无真实投递）
- `MessageServiceImpl` 仅做消息入库 + 附件 + 收件人；搜索 `SmsSender|MailSender|JavaMail|微信推送` 为 0 命中。
- **短信 / 邮件 / 微信 / 钉钉 等投递通道均未接入**，与文档"多通道通知"不符。

### F4. 文件（无对象存储）
- 搜索 `OSSClient|MinIO|S3Client|AliyunOss` 为 0 命中（仅 `FileDTO`）。
- 当前为**本地存储**，缺 MinIO / S3 / 阿里云 OSS 集成，不满足生产多副本、跨租户文件、大文件/断点续传需求。

### F5. 流程（引擎有、前端与管理能力缺）
- 后端已基于 **Flowable** 真实落地（部署/流程实例/任务，`WorkflowServiceImpl` 用到 `RepositoryService/RuntimeService/TaskService`）。
- 但：① **前端流程管理页是 mock，未打通**；② **无流程设计器（拖拽建模/BPMN 编辑）**；③ 流程引擎 DB 疑似 H2（`xcms-app/.../FlowableH2DatabaseTypeConfig`），生产需切到主库以保障持久化。

### F6. 运营 / 计量
- 运营看板为静态 mock，`OperationController` 虽有接口但是否聚合真实指标/配额需核实，前端未接。
- **缺计量（Metering）能力**：搜索 `metering|meterUsage` 为 0 命中（API 调用量计费、用量统计，Phase 3 未实现）。

### F7. 租户 / 部署
- **缺租户迁移（导出/导入）**：搜索 `tenant.migration|exportTenant|importTenant` 为 0 命中（Phase 3）。
- **缺独立部署开关**：搜索 `deploy.mode|standalone.deploy` 为 0 命中（多租户库 / 独立库切换，Phase 4 未实现）。
- 配额告警规则（`AlertRule`）已有，但是否联动限流/自动扩容需核实。

### F8. 已部分实现（确认无需作为缺口）
- 列脱敏：`ColumnMaskResponseBodyAdvice` + `ColumnMaskController` ✅
- 行级数据权限：`DataRuleController` + 相关拦截（需核实强制生效点）✅（部分）
- 跨租户授权令牌：`CrossTenantAuthController` / `CrossTenantResourceController` ✅（Phase 3 已落地）
- 任务调度引擎：`TaskSchedulerEngine` + `TaskLockService` + `@Scheduled` ✅（自研，非 Quartz）

### F9. 前端实时推送通道缺失（WebSocket / SSE 均未实现）
- 后端搜索 `WebSocket|@ServerEndpoint|STOMP|SockJS|SseEmitter|text/event-stream` 为 0 命中，项目为 Spring MVC（非 WebFlux），无服务端主动推送能力。
- 前端搜索 `WebSocket|EventSource|refetchInterval|setInterval` 为 0 命中，无推送客户端、无轮询兜底；当前页面为 mock 或一次性拉取。
- 影响：消息 / 待办 / 流程审批 / 告警等无法实时触达在线用户，只能手动刷新（或未来接 API 后轮询），体验与时效不达标；与 F3「消息仅入库不投递」是同一"消息投递"问题的两个侧面——F3 为**出站**通道（短信/邮件/微信）缺失，本项为**入站**通道（推送给在线用户）缺失。
- 建议：引入 WebSocket（Spring `WebSocketMessageBrokerConfigurer` + STOMP，或轻量 `WebSocketHandler`）或 SSE（`SseEmitter`）作为前端实时推送通道；消息落库后由 `DomainEventPublisher` 同时推送给订阅该 `tenantId`/`userId` 的会话；前端封装 `useNotification` 订阅。多租户下按 `tenantId` + `userId` 隔离订阅，并复用 `TenantContext`。

### 功能缺口优先级建议
1. **P0 打通前端**：把流程/消息/文件/审计/调度/运营 6 个模块页面接真实 API（后端已具备，性价比最高）。
2. **P0 安全补齐**：MFA、SSO 补全 SAML/微信钉钉、消息真实投递（短信/邮件/微信 + 前端实时推送 WebSocket/SSE）。
3. **P1 生产就绪**：文件 OSS、流程引擎持久化切主库、租户迁移、计量、独立部署开关。
4. **P2 增强**：流程设计器、跨租户用户切换、配额自动扩容。

---

> 注：以上问题基于对 `xcms-app` / `xcms-portal` / `xcms-identity` / `xcms-shared-kernel` / `xcms-authorization` 源码与 `docs` 文档的交叉核对。`ColumnMaskResponseBodyAdvice` 注释中提到"修复问题 3"，说明团队已有问题记录习惯，本文件可与既有流程对齐。
