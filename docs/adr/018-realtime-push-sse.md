# ADR-018: 实时推送采用 SSE + Redis pub/sub

- **Status**: Accepted
- **Date**: 2026-07-27

## Context

消息/待办/告警/流程审批无实时触达能力。后端搜索 `WebSocket`/`SseEmitter`/`STOMP` 为 0 命中（Spring MVC，无服务端推送）；前端搜索 `WebSocket`/`EventSource`/`refetchInterval` 为 0 命中（无推送客户端、无轮询兜底）。消息仅入库不投递（复盘 F9）。与 F3（消息仅站内信，无短信/邮件/微信出站通道）是"消息投递"问题的入站（推送给在线用户）与出站（多通道通知）两个侧面。

## Decision

优先 **SSE（Server-Sent Events）**，WebSocket 留待未来双向场景：

1. **协议选择**：消息/待办/告警/流程审批通知都是"服务器→用户"单向，SSE 足够且更轻（HTTP 长连接、自动重连、浏览器原生 `EventSource`）。WebSocket 留给协同编辑/实时对话等双向场景。
2. **后端**：Spring MVC `SseEmitter`，建立 `/api/notifications/stream`，按 `tenantId + userId` 订阅。
3. **触发链路**：业务事件（`MessageCreatedEvent`/`TodoAssignedEvent`/`AlertFiredEvent`）经 ADR-014 分发器 → 推送监听器 → 查找该用户在线 SSE 连接 → 写入。消息落库与推送解耦（落库由消息服务，推送由事件驱动）。
4. **多实例广播**：用户连接可能落在任意实例，需 **Redis pub/sub** 广播——推送监听器发布到 `notifications:{tenantId}:{userId}` channel，各实例订阅并推给自己持有的连接。与 ADR-012 无状态原则一致（连接态外置到 Redis + 本实例仅持句柄）。
5. **前端**：封装 `useNotification()` hook（`EventSource` 订阅 + 自动重连 + 降级轮询兜底），消息中心/待办角标实时更新。
6. **出站通道并行**：F3 短信/邮件/微信投递与 SSE 是"出站多通道"vs"入站在线推送"两个侧面，统一在 `MessageService` 多通道分发（站内信/SSE/短信/邮件）。
7. **单体零外部依赖**：单体单实例下 SSE 直接本实例推送，不依赖 Redis；集群/拆分时 `@ConditionalOnProperty` 启用 Redis pub/sub 广播。

## Alternatives

### 方案 B：WebSocket（STOMP）

- **优点**：双向，功能强
- **缺点**：单向推送场景过重，协议复杂，STOMP 需额外依赖；当前无双向需求

### 方案 C：前端轮询

- **优点**：零后端改造成本
- **缺点**：实时性差、无效请求多、体验不达标

## Consequences

### 正面

- 单向推送场景 SSE 轻量，浏览器原生支持，自动重连
- 事件驱动推送，与 ADR-014 事件基础设施协同
- 多实例 Redis pub/sub 广播，符合无状态原则

### 负面

- 集群需 Redis pub/sub（单体单实例可不依赖）
- SSE 连接管理（超时、心跳、断线重连）需完善
- `useNotification` hook + 降级轮询需开发

### 缓解措施

- 单体单实例阶段 SSE 直接推送，不引入 Redis
- 集群时 `@ConditionalOnProperty` 启用 Redis pub/sub
- 连接管理参考成熟 SSE 实践（心跳保活、Nginx 代理缓冲配置）
