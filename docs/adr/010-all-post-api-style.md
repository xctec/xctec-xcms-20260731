# ADR-010: 全 POST API 风格

- **Status**: Accepted
- **Date**: 2026-07-25

## Context

XCMS 需要确定 HTTP API 的设计风格。常见选择有 RESTful（GET/POST/PUT/DELETE）、RPC 风格（全 POST）等。

中台业务复杂，大量操作不是简单 CRUD（状态变更、流程审批、批量操作等），纯 RESTful 难以表达。同时，从安全角度希望最小化 HTTP 方法。

## Decision

采用 **全 POST** API 风格：

1. **所有接口显式使用 `@PostMapping`**，不使用 `@PutMapping`、`@DeleteMapping`
2. **禁用 PUT 和 DELETE** 方法
3. **GET 仅限必要场景**：文件下载、图片预览等流式响应场景可用 `@GetMapping`，但需在文档中说明理由
4. **所有参数通过 `@RequestBody` 传递**，不使用 query string、不使用 `@PathVariable`
5. **URL 命名**：`/{admin|api}/{module}/{action}`，kebab-case
6. **暂不加版本号前缀**，后续需要时再加

## Alternatives

### 方案 B：RESTful

- GET 查询、POST 创建、PUT 更新、DELETE 删除
- **优点**：标准、语义清晰
- **缺点**：复杂操作难表达（状态变更、流程审批）、PUT/DELETE 有安全风险

### 方案 C：RESTful + Action 端点

- CRUD 用 RESTful，复杂操作用 Action
- **优点**：兼顾标准与灵活
- **缺点**：两种风格混用，前端需判断何时用哪种方法

## Consequences

### 正面

- 安全性高，只暴露 POST
- 统一风格，前端不需要判断 HTTP 方法
- 所有参数在 body，不暴露在 URL
- 复杂操作表达自然（`/tenant/change-status` 比 `PATCH /tenant/{id}` 更直观）
- 批量操作简单（body 传数组）
- 防火墙/WAF 配置简单（只放行 POST）

### 负面

- 不符合 RESTful 规范
- 无法利用 HTTP 缓存（GET 可缓存，POST 不可）
- 浏览器直接访问 URL 无法调试（需 Postman 等）

### GET 的例外

以下场景允许使用 GET：
- 文件下载（流式响应）
- 图片/文件预览
- 其他需要浏览器直接访问的流式响应场景

使用 GET 时必须在代码注释中说明理由。
