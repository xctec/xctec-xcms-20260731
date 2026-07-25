# ADR-004: API/Impl 模块拆分模式

- **Status**: Accepted
- **Date**: 2026-07-24

## Context

单体多模块架构中，模块间需要互相调用（如 workflow 需要查用户、校验权限）。如何组织模块间的接口定义和依赖关系，决定了未来能否平滑拆分为微服务。

核心问题：跨模块接口定义放在哪里？

## Decision

每个业务模块拆分为 **api（契约）** 和 **impl（实现）** 两个子模块：

- `xxx-api`：接口定义 + DTO + 事件类（轻量，无实现）
- `xxx-impl`：实现 + JPA实体 + Repository + Controller

依赖规则：
- `*-api` 可依赖 `shared-kernel` 和其他 `*-api`
- `*-impl` 依赖自己的 `*-api` + 其他 `*-api` + `shared-kernel`
- **`*-impl` 绝不依赖其他 `*-impl`**（关键约束）
- `app` 依赖所有 `*-impl`，负责组装

`shared-kernel` 只放全局共享基础设施（TenantContext、基础实体、工具类），**不放任何业务接口**。

## Alternatives

### 方案 B：所有接口放 shared-kernel

- **优点**：简单
- **缺点**：shared-kernel 膨胀，变成"什么都知道"的上帝模块，失去模块化意义

### 方案 C：直接依赖 impl 模块

- **优点**：无需拆分
- **缺点**：impl 间互相依赖，无法拆分为微服务，循环依赖风险

## Consequences

### 正面

- 模块边界清晰，impl 间解耦
- 从单体到微服务：api 不变，消费者代码不变，只换注入的实现
- shared-kernel 保持最小化

### 负面

- 模块数量翻倍（每个模块两个子模块）
- 需要维护接口和实现的分离

### 微服务演进

单体时：Spring 注入 `xxx-impl` 的真实实现（方法调用）
微服务时：Spring 注入 `xxx-client` 的远程实现（HTTP/Feign 调用）
消费者代码完全不变。
