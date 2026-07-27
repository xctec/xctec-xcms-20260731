# ADR-014: 事件驱动消费端抽象与事务性 Outbox

- **Status**: Accepted
- **Date**: 2026-07-27

## Context

事件发布端是干净的六边形端口（`DomainEventPublisher` 接口 + `DomainEvent` 契约，`InProcessEventPublisher`↔`MqEventPublisher` 可 `@ConditionalOnMissingBean` 切换）。但**消费端无抽象**——各监听器裸 `@Component` + `@EventListener`，租户切换/重试/幂等各写各的（有的 `switchTo(event.getTenantId())`，有的不切）；`MqEventPublisher` 无 `@Bean` 装配，永远同步进程内；无 Outbox，崩溃丢事件；无 `@EnableAsync`；消费端无 `eventId` 去重（复盘 S7/S10）。

部署路线为混合部署，要求**单体零外部依赖（不引入 MQ），拆分时按需切换 MQ**，且选型无关。

## Decision

### 1. 补入站事件端口，使发布/消费对称

```java
public interface DomainEventListener<E extends DomainEvent> {
    void onEvent(E event);
    Class<E> eventType();
}
```

各监听器实现它 + `@Component`。引入 `DomainEventDispatcher` 统一分发器：收集所有 `DomainEventListener` Bean 按 `eventType()` 路由。进程内由单个 `@EventListener(DomainEvent.class)` 委托分发器；MQ 模式由单个消费者反序列化后同样委托。**横切集中到分发器**：
- `ActorContext.switchTo(event.getTenantId())`（消除各监听器不一致）
- `eventId` 幂等去重（去重表/Redis SETNX）
- 失败重试/死信
- 业务监听器只写 `onEvent` 纯逻辑，可单测

### 2. 选型无关 SPI（单体不引入 MQ）

`DomainEventPublisher`（出站）+ `DomainEventListener`/`DomainEventDispatcher`（入站）双抽象，进程内↔MQ 由 `@ConditionalOnProperty` 切换：
- **单体**：`InProcessEventPublisher` + 同步/`@Async` 分发，**不引入 MQ**
- **拆分**：装配 `MqEventPublisher` + MQ 消费者，业务监听器代码不变

### 3. 短期（单体多实例前）

- 监听器改 `@TransactionalEventListener(AFTER_COMMIT)`（事务提交后才处理，避免回滚丢事件）
- 可异步监听器加 `@Async` + `@EnableAsync` + `TaskDecorator` 传播 `ActorContext`
- 消费端按 `eventId` 去重

### 4. 中期（微服务/跨服务前必做）

- **事务性 Outbox**：事件随业务事务落 `event_outbox` 表（同库同事务），后台调度轮询转发 MQ → at-least-once，业务回滚则事件不发出
- `MqEventPublisher` 真正装配为 `@ConditionalOnProperty`，配齐 serializer/sender（Kafka 或 RabbitMQ，选型推迟到拆分时）
- 补重试 + 死信队列
- `DomainEvent` 增 `version()`，统一 `topic()` 命名规范（`xcms.<module>.<event>`）

## Alternatives

### 方案 B：保留裸 `@EventListener`，不抽象消费端

- **优点**：零改造成本
- **缺点**：租户/重试/幂等各监听器各写各的，无法一处治理；迁 MQ 只能逐监听器手工接消费端

### 方案 C：直接引入 Spring Cloud Stream

- **优点**：现成的发布/消费抽象
- **缺点**：单体阶段强制依赖 MQ/绑定器，违反"单体零外部依赖"；绑定器抽象与现有 `DomainEventPublisher` 重复

## Consequences

### 正面

- 发布/消费对称，进程内↔MQ 无缝切换，业务监听器零改动
- 租户传播/幂等/重试集中治理，消除各监听器不一致
- Outbox 保证 at-least-once，崩溃不丢事件
- 单体不引入 MQ，拆分时按需切换

### 负面

- 引入分发器 + Outbox 表 + 后台转发器，初期工作量
- 团队需理解 `DomainEventListener` 端口约定

### 缓解措施

- 短期只做消费端抽象 + `AFTER_COMMIT` + `eventId` 去重（不引入 Outbox/MQ）
- Outbox/MQ 推迟到拆分前，`@ConditionalOnProperty` 渐进启用
- 是 ADR-015（租户初始化）、ADR-016（异步任务完成事件）、ADR-018（推送）、审计埋点的共同底座，优先级最高
