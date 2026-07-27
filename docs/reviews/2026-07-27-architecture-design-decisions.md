# 架构设计决策 — 微服务就绪基础设施

> 文档定位：设计决策纲领（Design Decisions）。基于阶段复盘（`docs/reviews/2026-07-27-milestone-retrospective.md`）与架构评审（`docs/code-review/2026-07-27-stage-architecture-review.md`）上升的设计议题。
> 日期：2026-07-27
> 落点：本文件为纲领，每节将沉淀为独立 ADR（ADR-011 ~ ADR-016）。
> 原则：**单体可跑、微服务可拆**——所有决策在单体形态下不增加运行成本，但为 Phase 4 拆分/独立部署铺路。

## 0. 背景与总原则

复盘确认架构底子扎实（模块化 api/impl、租户隔离、OSIV 关闭），但存在一类系统性问题：**多处实现假设"单进程、单实例、同步"**——自研鉴权、本地缓存、内存任务句柄、同步事件、拉取式回写。单体下能跑，多实例/拆服务即崩。

八项设计诉求归为三条主线：
- **安全与身份主线**（诉求 1、4）：Spring Security 迁移 + 无状态 + 调用规范 → 数据权限下沉（诉求 3）
- **事件与异步主线**（诉求 2、7、8）：消费端抽象 + Outbox → 租户初始化契约化 + 异步任务多实例
- **可观测与实时主线**（诉求 5、6）：Prometheus 栈 + SSE 推送

**部署路线（已确认）**：混合部署——**单体 → 拆分部分独立 → 混合**并存。所有 SPI 设计必须满足：**单体形态零外部依赖可跑，拆分/集群时按 `@ConditionalOnProperty` 引入外部组件**。这是贯穿下文的硬约束。

总原则五条，贯穿所有决策：
1. **无状态服务**：实例可任意启停/扩缩/非 sticky，任何实例级可变状态必须外置（Redis/DB/OSS）。
2. **身份与租户上下文显式传播**：不依赖 ThreadLocal 跨线程/跨进程隐式继承；事件/异步/出站调用显式携带。
3. **横切能力下沉 kernel**：数据权限、审计、租户上下文等"所有服务都需要"的能力，放 shared-kernel 或独立薄模块，禁止让业务服务依赖 auth/identity 的 impl。
4. **发布/消费对称抽象**：事件发布已抽象（`DomainEventPublisher`），消费端必须对称抽象，使进程内↔MQ 可无缝切换。
5. **外部组件选型无关 SPI**：MQ、缓存、广播、锁等外部组件一律抽象为端口（`DomainEventPublisher`/`CachePort`/`PubSubPort`/`DistributedLockPort`），单体用进程内默认实现（Caffeine/同步事件/本地锁），集群/拆分时 `@ConditionalOnProperty` 切换 Redis/MQ 实现，业务代码零改动。

## 1. 鉴权体系：迁移 Spring Security（诉求 1）→ ADR-011

**现状问题**：自研 `TenantInterceptor` + `JwtTokenProvider` + `JwtTenantResolver`，无方法级权限、无 CSRF/OAuth2 Resource Server 生态兜底；`@PreAuthorize` 不可用；权限判断散落，强依赖前端 `<Can>`。

**设计决策**：迁移到 **Spring Security 6 Resource Server（JWT）**，鉴权与租户解析职责分离。

**方案要点**：
- `SecurityFilterChain`：`oauth2ResourceServer().jwt()` 校验 JWT 签名/exp，替代 `TenantInterceptor` 的 token 校验职责。`TenantInterceptor` 退化为仅"从已认证 `Authentication` 提取 tenantId/userId 写入 `TenantContext`"。
- 方法级权限：`@EnableMethodSecurity` + `@PreAuthorize("hasAuthority('user:create')")`，后端强制校验，不依赖前端 `<Can>`。
- 权限来源：`JwtAuthenticationConverter` 从 JWT claim（或登录后下发的权限码）构建 `GrantedAuthority`；配合 `PermissionService` 缓存。
- 登录/SSO 仍由 `AuthController`/`SsoController` 发 token，但校验侧由 Spring Security 统一。
- 保留 `PasswordEncoder`（已在用）。

**依赖**：无前置。是诉求 4（调用规范）的技术底座。

**风险**：迁移期需保持 `TenantInterceptor` 兼容（租户解析不能一步切走）；方法级注解需逐 controller 补，可分批。

## 2. 无状态服务与微服务调用规范（诉求 4）→ ADR-012

**现状问题**：`TenantContext`（ThreadLocal）仅入口拦截器填充，无出站传播；`refreshToken` 存 localStorage（XSS）；拆服务后首跳即断链；定时任务/MQ 消费者无 JWT，`userId` 为 null。

**设计决策**：明确"无状态服务"为架构原则，区分**用户请求**与**系统调用**两种身份，定义出站上下文传播规范。

**方案要点**：
- **身份模型二分**：
  - **用户请求**：前端持 JWT access token（短期，如 15min）+ refresh token 走 httpOnly cookie（不进 localStorage/JS）。
  - **系统调用**（调度/MQ 消费/服务间）：签发**服务令牌**（service account JWT，含 `tenantId` + `service principal`，如 `system@task-scheduler`），由各服务自行签发或统一 token 服务发放。
- **出站上下文传播**：定义 `ClientHttpRequestInterceptor`（RestTemplate）/ `RequestInterceptor`（Feign）/ `ExchangeFilterFunction`（WebClient），统一注入：
  - 用户请求转发：原 `Authorization: Bearer <user-jwt>`
  - 系统调用：`Authorization: Bearer <service-jwt>` + 内部头 `X-Tenant-Id`/`X-User-Id`（仅系统调用用，签名防伪）
- **入口解析**：目标服务 `TenantInterceptor`（或 Spring Security filter）同时支持"用户 JWT"与"内部服务令牌 + 签名头"两种解析路径。
- **网关注入**（中期）：网关统一校验用户 JWT，注入内部签名租户头；内部服务间信任签名头（配合 mTLS），减少逐跳解析 JWT。
- **`TenantContext` 更名**：实为"调用者身份上下文"，更名 `ActorContext`（拆 `tenantId` + `principal{userId/serviceName}`），删除死字段 `dataSourceKey`（见复盘 S12）。
- **异步传播**：`@Async` 线程池配 `TaskDecorator` 显式传播 `ActorContext`；事件自带 `tenantId`（见 §4）。

**依赖**：诉求 1（Spring Security 提供服务令牌签发/校验基础）。是诉求 3（数据权限跨服务）的前置。

## 3. 数据权限下沉：脱离 auth 依赖（诉求 3）→ ADR-013

**现状问题**：行级数据范围（`DataRuleService`）与列级脱敏（`ColumnMaskResponseBodyAdvice`）都在 `xcms-auth-impl`，微服务下要么所有服务依赖 auth-impl（违反模块边界），要么不依赖即失效。脱敏是 HTTP 出口切面，跨服务/聚合层 fail-open（复盘 S13）。

**设计决策**：数据权限作为**横切能力下沉**到独立薄模块 `xcms-data-permission`（仅依赖 shared-kernel + 自身 api），所有服务天然具备，不依赖 auth-impl。

**方案要点**：
- **模块定位**：`xcms-data-permission-api`（SPI/注解/契约）+ `xcms-data-permission-impl`（默认实现：本地缓存规则 + 拦截）。auth-impl 只管"权限/规则的 CRUD 管理"，运行时拦截由 data-permission 提供。
- **行级数据范围**：
  - 定义 SPI `DataPermissionRuleProvider`（按 `resourceType` 返回规则），各业务模块**自行实现注册**（不依赖 auth）。auth-impl 的 `DataRuleServiceImpl` 改为实现该 SPI 的"管理面提供者"。
  - 拦截点：Hibernate filter 或 Specification 包装器，在 `xcms-data-permission-impl` 提供基类/拦截器，业务 repository 继承即生效。
- **列级脱敏**：
  - 从 `ResponseBodyAdvice`（HTTP 出口、依赖标注、fail-open）**下沉到 DTO 映射层**：`@MaskField(resourceType, field)` 注解 + MapStruct/序列化增强，`toDTO` 后立即脱敏。
  - **fail-closed**：缺规则/tenantId 为 null 时默认脱敏或拒绝，不裸奔。
  - 解除 `ApiResponse` 耦合，脱敏在 DTO 层对所有返回类型生效。
- **跨服务语义**：内部服务调用不脱敏（保留数据完整性），仅"面向最终用户的出网边界"（BFF/网关/Controller 出口）脱敏。靠 §2 的"系统调用"身份标识区分。
- **规则缓存**：本地 Caffeine 改 Redis（见 §6 无状态），`PermissionChangedEvent` 经 §4 事件广播失效。

**依赖**：§2（跨服务上下文传播，脱敏需知 tenantId/userId/角色）。auth-impl 的职责收敛需与 §1 协同。

## 4. 事件机制：补消费端抽象与可靠投递（诉求 2）→ ADR-014（核心底座）

**现状问题**：发布端是干净六边形端口（`DomainEventPublisher`），**消费端无抽象**——各监听器裸 `@EventListener`，租户切换/重试/幂等各写各的；`MqEventPublisher` 无装配，永远同步进程内；无 Outbox，崩溃丢事件（复盘 S7/S10）。

**设计决策**：补**入站事件端口** `DomainEventListener<E>` + 统一分发器，使发布/消费对称；落地事务性 Outbox 保证 at-least-once。

**方案要点**：
- **入站端口**：
  ```java
  public interface DomainEventListener<E extends DomainEvent> {
      void onEvent(E event);
      Class<E> eventType();
  }
  ```
  各监听器实现它 + `@Component`。
- **统一分发器** `DomainEventDispatcher`：收集所有 `DomainEventListener` Bean 按 `eventType()` 路由。进程内：单个 `@EventListener(DomainEvent.class)` 委托分发器；MQ 模式：单个消费者反序列化后同样委托。**横切集中**：
  - `ActorContext.switchTo(event.getTenantId())`（消除各监听器不一致）
  - `eventId` 幂等去重（去重表/Redis SETNX）
  - 失败重试/死信
  - 业务监听器只写 `onEvent` 纯逻辑，可单测。
- **短期（单体多实例前）**：
  - 监听器改 `@TransactionalEventListener(AFTER_COMMIT)`（事务提交后才处理，避免回滚丢事件）。
  - 可异步监听器加 `@Async` + `@EnableAsync` + `TaskDecorator` 传播 `ActorContext`。
  - 消费端按 `eventId` 去重。
- **中期（微服务/跨服务前必做）**：
  - **事务性 Outbox**：事件随业务事务落 `event_outbox` 表（同库同事务），后台调度轮询转发 MQ → at-least-once，业务回滚则事件不发出。
  - `MqEventPublisher` 真正装配为 `@ConditionalOnProperty`，配齐 serializer/sender（Kafka 或 RabbitMQ）。
  - 补重试 + 死信队列。
- **事件版本化**：`DomainEvent` 增 `version()`，统一 `topic()` 命名规范（如 `xcms.<module>.<event>`）。

**依赖**：无前置，是底座。支撑诉求 5（租户初始化）、6（异步任务）、6'（推送）、审计埋点。

**选型无关保证**：`DomainEventPublisher`（出站）+ `DomainEventListener`/`DomainEventDispatcher`（入站）双抽象，进程内↔MQ 由 `@ConditionalOnProperty` 切换。**单体不引入 MQ**，用 `InProcessEventPublisher` + 同步/`@Async` 分发；拆分时装配 `MqEventPublisher` + MQ 消费者，业务监听器代码不变。Outbox 表始终启用（单体下后台转发器可关闭或转发到进程内 dispatcher），保证迁移平滑。

## 5. 租户初始化：事件驱动契约化（诉求 7）→ ADR-014 子节 / ADR-015

**现状问题**：租户初始化靠 `TenantCreatedEvent` + 各 `*Initializer` 监听器，但未契约化文档化；`@EventListener`（非 AFTER_COMMIT）+ 复用外层会话导致 `@TenantId` 串租户（复盘 S1）；失败回滚整个创建。

**设计决策**：继续事件驱动，但**契约化 + 修时序**。

**方案要点**：
- **契约清单**（落设计文档）：`TenantCreatedEvent` 触发后，各模块声明自己的初始化职责：
  | 模块 | 初始化内容 |
  |---|---|
  | identity | 管理员用户（随机密码 + 首登改密）+ 默认角色 |
  | organization | 根部门 + 默认岗位 |
  | authorization | 默认菜单分配 + 角色权限绑定 |
  | message/config | 默认消息模板/参数 |
  | ... | 按产品定义补充 |
- **时序修复**：监听器 `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` + `Propagation.REQUIRES_NEW`，在新事务/新会话中执行，`ActorContext.switchTo(newTenantId)` 在会话开启前生效，`@TenantId` 正确捕获新租户。
- **幂等**：每个初始化器按 `(tenantId, 初始化项)` 去重（已有 `findByUsername().isEmpty()` 检查，补显式 `tenantId` 查询）。
- **失败补偿**：AFTER_COMMIT 后初始化失败不回滚租户创建，记录 `tenant_init_status` 表，可单独重试补偿。
- **种子数据扩充**：补默认菜单/权限分配/岗位等（复盘 S5）。
- **新租户密码**：随机生成 + 首登强制改密，日志不打印明文（复盘 S3）。

**依赖**：§4（事件消费端抽象提供 AFTER_COMMIT/REQUIRES_NEW/幂等/重试基础设施）。

## 6. 异步任务执行：多实例就绪（诉求 8）→ ADR-016（降优先级）

> **优先级调整（已确认）**：任务调度开发优先级降到最低，**暂不引入 XXL-Job/PowerJob**，不自研多实例调度。当前单实例 `TaskSchedulerEngine` 在混合部署的单体阶段够用；待真正拆分/集群时再按本节方案升级。本节作为"未来就绪"设计保留，不阻塞当前迭代。

**现状问题**：`TaskSchedulerEngine` 用 `ConcurrentHashMap<Long, ScheduledFuture>` 内存句柄，仅 leader 实例有运行态，宕机丢句柄、无跨实例接管（复盘 S9）。

**设计决策**：运行态落 DB + 保留 DB 锁 leader 选举，短期不引入外部调度器；中期评估 XXL-Job/PowerJob。

**方案要点**：
- **任务元数据已落 DB**（`task_definition`/`task_schedule`），保留。
- **运行态落 DB**：新增 `task_runs` 表（`task_id, started_at, finished_at, status, instance_id, result`），每次执行写记录；内存 `futures` 仅作本实例取消句柄，不作跨实例可见性依据。
- **leader 选举已 DB 化**（`TaskLockService` 行锁）：多实例只有 leader 调度，leader 宕机后 DB 锁释放，备实例接管。补健康探活 + 锁续约。
- **`@Async` 业务任务**：线程池 + `TaskDecorator` 传播 `ActorContext`（系统触发用 service principal，见 §2）。
- **幂等**：`task_runs` 按 `(task_id, scheduled_fire_time)` 唯一约束防重复执行。
- **中期评估**：若任务量大/需可视化，迁移 XXL-Job/PowerJob（自研引擎作过渡）。决策点：自研满足则不引入。
- **跨服务任务回调**：任务完成后发 `TaskCompletedEvent`（经 §4 Outbox），业务模块订阅，不在引擎内直接调业务。

**依赖**：§2（系统触发身份）、§4（完成事件）。

## 7. 可观测性：Prometheus + Grafana + Alertmanager（诉求 5）→ ADR-017

**现状问题**：指标 push 式落 `metric_value` 关系库（高基数写 OLTP、无实例维度、多实例错乱）；告警自建玩具引擎，`durationMin` 形同虚设，无持续/抑制/路由（复盘 S15）。

**设计决策**：迁移业界标准可观测栈，废弃自建指标落库与告警引擎。

**方案要点**：
- **采集**：加 `micrometer-registry-prometheus`，暴露 `/actuator/prometheus`。`MetricProvider` SPI 语义从"落 DB"改为"向 Micrometer 注册 `Meter`（Counter/Gauge/Timer）"，业务指标自动进 Prometheus。JVM/缓存指标由 actuator 自动暴露。
- **存储/可视化**：Prometheus 拉取 + Grafana 仪表盘（多租户以 `tenant` label 过滤；租户基数大时仅关键租户打 label，或远程写分片）。
- **告警**：PromQL + `for: 5m` 持续时间 + 分组/抑制/静默 + 多通道（邮件/钉钉/Webhook），用 Alertmanager。`AlertRule` 表改为消费 Alertmanager webhook 归档 `alert_record`，不自算阈值——`durationMin` 终于生效。
- **废弃**：移除 `metric_value` 落库；`MetricServiceImpl.collect()` 改为注册 Meter 或作自定义聚合兜底。
- **解决**：F6 运营看板静态 mock → Grafana 嵌入/数据源替代。

**依赖**：相对独立。与 §2 多实例 service discovery 协同（Prometheus 自动发现实例）。

## 8. 实时推送：SSE 优先（诉求 6）→ ADR-018

**现状问题**：消息/待办/告警无实时触达，前端无推送客户端/无轮询（复盘 F9）；消息仅入库不投递。

**设计决策**：优先 **SSE**（服务器→客户端单向推送），WebSocket 留待未来双向场景。

**方案要点**：
- **协议选择**：消息/待办/告警/流程审批通知都是"服务器→用户"单向，SSE 足够且更轻（HTTP 长连接、自动重连、浏览器原生 `EventSource`）。WebSocket 留给协同编辑/实时对话等双向场景。
- **后端**：Spring MVC `SseEmitter`（或 WebFlux `Flux<ServerSentEvent>` 若迁响应式）。建立 `/api/notifications/stream`，按 `tenantId + userId` 订阅。
- **触发链路**：业务事件（`MessageCreatedEvent`/`TodoAssignedEvent`/`AlertFiredEvent`）经 §4 分发器 → 推送监听器 → 查找该用户在线 SSE 连接 → 写入。消息落库与推送解耦（落库由消息服务，推送由事件驱动）。
- **多实例广播**：用户连接可能落在任意实例，需 **Redis pub/sub** 广播——推送监听器发布到 `notifications:{tenantId}:{userId}` channel，各实例订阅并推给自己持有的连接。与 §1 无状态原则一致（连接态外置到 Redis + 本实例仅持句柄）。
- **前端**：封装 `useNotification()` hook（`EventSource` 订阅 + 自动重连 + 降级轮询兜底），消息中心/待办角标实时更新。
- **出站通道并行**：F3 短信/邮件/微信投递与 SSE 是"出站多通道"vs"入站在线推送"两个侧面，统一在 `MessageService` 的多通道分发（站内信/SSE/短信/邮件）。

**依赖**：§4（事件分发驱动推送）、§1 无状态（Redis pub/sub 广播）。

## 9. 依赖关系与推进顺序

```
主线A（安全身份）:  [1 Spring Security] → [2 无状态/调用规范] → [3 数据权限下沉]
主线B（事件异步）:  [4 事件消费端抽象+Outbox] → [5 租户初始化契约]
                                          ↘ [6 异步任务多实例]
                                          ↘ [8 SSE 推送]
                                          ↘ 审计埋点(复盘S6)
主线C（可观测）:    [7 Prometheus 栈]（独立，可与任意主线并行）
```

**推进建议**：
1. **第一批（底座，并行）**：§4 事件消费端抽象（主线B底座）+ §1 Spring Security 迁移（主线A底座）。两者无相互依赖，可并行。
2. **第二批**：§2 无状态/调用规范（依赖§1）+ §5 租户初始化契约（依赖§4）。
3. **第三批**：§3 数据权限下沉（依赖§2）+ §6 异步任务（依赖§2/§4）+ §8 SSE 推送（依赖§4）+ §7 Prometheus（独立）。

每批完成后单体仍可跑（不引入外部依赖的改造先行；Outbox/MQ/Redis 等外部组件用 `@ConditionalOnProperty` 渐进启用）。

## 10. ADR 落点

| ADR | 主题 | 诉求 |
|---|---|---|
| ADR-011 | 鉴权体系迁移 Spring Security | 1 |
| ADR-012 | 无状态服务与微服务调用规范 | 4 |
| ADR-013 | 数据权限横切下沉 | 3 |
| ADR-014 | 事件驱动：消费端抽象 + Outbox | 2（含 5 租户初始化、审计埋点） |
| ADR-015 | 租户初始化事件契约 | 7 |
| ADR-016 | 异步任务多实例就绪 | 8 |
| ADR-017 | 可观测性：Prometheus + Grafana + Alertmanager | 5 |
| ADR-018 | 实时推送：SSE + Redis pub/sub | 6 |

## 11. 待确认事项决策结果（2026-07-27）

| # | 事项 | 决策 |
|---|---|---|
| 1 | 微服务拆分边界 | **混合部署**：单体 → 拆分部分独立 → 混合并存。SPI 设计满足"单体零外部依赖、拆分按需引入"。不预先按某拆分蓝图过度设计，按实际拆分节点渐进。 |
| 2 | MQ 选型 | **选型无关 SPI**：`DomainEventPublisher`/`DomainEventListener` 抽象，单体用进程内（不引入 MQ），拆分时 `@ConditionalOnProperty` 装配具体 MQ。选型推迟到真正拆分时定。 |
| 3 | 缓存/广播等外部组件 | **抽端口 SPI**：`CachePort`/`PubSubPort`/`DistributedLockPort`，单体用本地（Caffeine/进程内/DB 锁），集群必须引入 Redis（`@ConditionalOnProperty` 切换）。单体不强制依赖 Redis。 |
| 4 | 任务调度是否引入 XXL-Job/PowerJob | **暂不引入**，任务调度开发优先级降到最低。当前单实例够用，待拆分/集群再升级（见 §6）。 |
| 5 | data-scope vs data-rule | **统一为 data-rule（后端实际模型），data-scope 作为简化预设 UI**。详见 §12。 |

## 12. data-scope vs data-rule：模型统一决策（诉求 5 解析）

### 12.1 两者本质区别

经核对后端实现（`DataRule` 实体 / `DataPermissionServiceImpl` / `DataRuleController`）与前端 Permission 页，两者是**粒度与模型完全不同**的两套数据权限：

| 维度 | data-scope（前端臆造） | data-rule（后端实际实现） |
|---|---|---|
| **模型** | 角色级粗粒度枚举 | 规则级细粒度配置 |
| **配置单元** | 一个角色一个 `scopeType` | 一个角色多条 `DataRule`（多对多 + scopeValue） |
| **取值** | `ALL`/`SELF`/`CURRENT_DEPT`/`DEPT_AND_CHILD`/`CUSTOM` 5 个固定枚举 | `dimension`：`OWNER`/`ORG`/`BUSINESS_LINE`/`REGION`/`TAG`/`TIME`，每条配 `ruleConfig` |
| **作用对象** | 角色全局（不分资源类型） | 按 `resourceType` 差异化（用户管理看本部门，订单看本业务线） |
| **运行时** | **后端不读**（前端调臆造 `/admin/authz/data-scope*`，后端无此端点） | `DataPermissionService.getDataPermissionContext` 汇总角色规则 → `getDataScopeSpec` 生成 JPA Specification 行级过滤 |
| **存储** | 前端 `{scopeType, scopeValues}`（mock） | `perm_data_rule` + `perm_data_rule_role`（真实落库） |

### 12.2 语义重叠与差异

- `SELF` ≈ data-rule 的 `OWNER` 维度（`createdBy = userId`）
- `CURRENT_DEPT`/`DEPT_AND_CHILD` ≈ data-rule 的 `ORG` 维度（`orgPath like ?%`）
- `ALL` ≈ data-rule 无规则（返回 `conjunction` 全量）
- **data-rule 多出**：`BUSINESS_LINE`/`REGION`/`TAG`/`TIME` 四个维度，data-scope 无法表达
- **data-scope 的 `CUSTOM`**：本意是"自定义"，恰好对应 data-rule 的多维度规则编辑

### 12.3 优缺点

**data-scope（粗粒度枚举）**
- ✅ 简单直观，用户选"本部门"即可，配置成本低
- ✅ 适合标准组织树驱动的中台，权限需求简单
- ❌ 只能按组织/本人维度，无法支持业务线/地区/标签/时间
- ❌ 一个角色一个范围，无法按资源类型差异化
- ❌ 当前是前端臆造，后端不读，联调即 404

**data-rule（细粒度规则）**
- ✅ 多维度、按资源类型差异化、可组合，灵活强大
- ✅ 后端已实现运行时过滤（Specification）+ 管理 API（`/admin/data-rule/*`）
- ❌ 配置复杂（要理解 dimension + ruleConfig），用户门槛高
- ❌ 需规则管理 UI（后端有 controller，前端未接）

### 12.4 决策：统一为 data-rule，data-scope 作预设 UI

两者不是互斥，是**层次关系**。统一存储用 data-rule，前端提供"快速模式（data-scope 预设）+ 高级模式（data-rule 编辑）"两种 UI：

| data-scope 预设 | 映射为 data-rule |
|---|---|
| `ALL` | 清除该角色所有规则（无规则=全量） |
| `SELF` | 生成一条 `OWNER` 规则（`ownerField=createdBy`） |
| `CURRENT_DEPT` | 生成一条 `ORG` 规则（`ruleConfig=当前部门 path`） |
| `DEPT_AND_CHILD` | 生成一条 `ORG` 规则（`ruleConfig=本部门 path 前缀`，利用 `like ?%`） |
| `CUSTOM` | 展开为 data-rule 多维度规则编辑器（ORG/BUSINESS_LINE/REGION/TAG/TIME） |

**收益**：
- 前端保留简化体验（90% 场景选预设即可），后端统一一套数据权限引擎
- 消除前端臆造 `/admin/authz/data-scope*`，改对接真实 `/admin/data-rule/*`
- `CUSTOM` 复杂场景有出口（多维度组合）
- 后端 `DataPermissionService` 运行时逻辑零改动（本就读 data-rule）

**前端改造**：Permission 页"数据范围"区块，RadioGroup 选预设 → 调 `data-rule` 接口生成/清除对应规则；选 `CUSTOM` → 展开 data-rule 维度编辑器。删除 `authzApi.getDataScope/updateDataScope` 臆造方法。

**关联**：与 §3 数据权限下沉协同——data-rule 的管理（CRUD）仍由 auth 模块，运行时拦截（`getDataScopeSpec`）下沉到 `xcms-data-permission` 供所有服务复用。

---

> 本纲领确认后，按 §9 顺序逐个落 ADR-011~018，每个 ADR 含背景/决策/方案/影响/迁移路径。建议先开 ADR-014（事件底座，杠杆最高，支撑多项）与 ADR-011（Spring Security，安全基线）。
