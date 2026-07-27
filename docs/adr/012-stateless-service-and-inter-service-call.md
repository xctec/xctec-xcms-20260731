# ADR-012: 无状态服务与微服务调用规范

- **Status**: Accepted
- **Date**: 2026-07-27

## Context

`TenantContext`（ThreadLocal）仅由 `xcms-portal` 的 `TenantInterceptor` 在入口填充，**无出站传播**。单体下所有模块同进程同线程，下游 `@Autowired` 调用天然继承，能跑。但拆分后服务 A→B 的 HTTP 调用若不带凭证，B 的拦截器直接 401；定时任务/MQ 消费者无 JWT，`getCurrentUserId()` 返回 null，审计/创建人落 null（复盘 S8/S12）。

`refreshToken` 存 localStorage（XSS 可窃取长效凭证）。`TenantContext` 实为"调用者身份上下文"却名为 Tenant，承载 userId/dataSourceKey（死字段），命名误导。

部署路线已确认为混合部署（单体→部分拆分→混合并存），需明确调用规范使拆分不断链。

## Decision

明确**无状态服务**为架构原则，区分**用户请求**与**系统调用**两种身份，定义出站上下文传播规范：

1. **身份二分**：
   - 用户请求：前端持 JWT access token（短期 15min）+ refresh token 走 **httpOnly cookie**（不进 localStorage/JS）
   - 系统调用（调度/MQ 消费/服务间）：签发**服务令牌**（service account JWT，含 tenantId + service principal，如 `system@task-scheduler`）
2. **出站上下文传播**：定义 `ClientHttpRequestInterceptor`（RestTemplate）/ `RequestInterceptor`（Feign）/ `ExchangeFilterFunction`（WebClient），统一注入：
   - 用户请求转发：原 `Authorization: Bearer <user-jwt>`
   - 系统调用：`Authorization: Bearer <service-jwt>` + 内部签名头 `X-Tenant-Id`/`X-User-Id`（防伪）
3. **入口解析**：目标服务同时支持"用户 JWT"与"内部服务令牌 + 签名头"两种解析路径。
4. **`TenantContext` 更名 `ActorContext`**：拆 `tenantId` + `principal{userId/serviceName}`，删除死字段 `dataSourceKey`。
5. **异步传播**：`@Async` 线程池配 `TaskDecorator` 显式传播 `ActorContext`；事件自带 `tenantId`（见 ADR-014）。
6. **单体零外部依赖**：出站拦截器在单体下不生效（无跨服务调用），拆分时 `@ConditionalOnProperty` 启用。

## Alternatives

### 方案 B：仅转发用户 JWT 到所有服务

- **优点**：简单，一种凭证
- **缺点**：系统调用（调度/MQ）无用户 JWT，需伪造用户 token；token 中途过期；"用户身份"强加给"系统调用"信任混淆

### 方案 C：网关全权鉴权，内部服务无鉴权信任内网

- **优点**：内部服务简单
- **缺点**：内网被突破即全失守；无法区分"用户直达"与"系统调用"

## Consequences

### 正面

- 拆分后跨服务调用不断链，系统调用有独立可信身份
- `refreshToken` 移出 localStorage，XSS 风险降低
- `ActorContext` 命名准确，传播面清晰（不含死字段）

### 负面

- 出站拦截器需实现并维护（3 种 HTTP 客户端）
- 服务令牌签发/校验需配套（可复用 ADR-011 的 Spring Security）
- httpOnly cookie refresh 需前后端协同改造

### 缓解措施

- 单体阶段不启用出站拦截器（`@ConditionalOnProperty`），零成本
- 服务令牌签发复用 `JwtTokenProvider`，校验复用 Spring Security
- refresh cookie 改造与 ADR-011 协同推进
