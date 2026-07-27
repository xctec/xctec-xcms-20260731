# XCMS — 集团中台技术底座

> **状态：建设中（MVP 阶段）** — 后端核心能力已具雏形、前端部分打通，生产就绪与高级能力仍在推进。
> 本文档聚焦**项目特点**、技术栈、架构与**建设计划 Checklist**（已完成项已勾选）。

---

## 一、项目定位与特点

XCMS 是面向**集团型 / 多组织**的中台系统，作为上层业务系统的**通用能力底座**，提供多租户隔离、统一身份与权限、组织与数据权限、审计、流程、消息、文件、配置、调度等能力。

**核心特点（差异化于一般后台脚手架）：**

1. **模块化单体（Modular Monolith）** — 按业务边界划分模块，每个模块 = `api`（契约/接口/DTO/事件/枚举）+ `impl`（实现）两个子模块，契约与实现分离、可独立演进（ADR-004）。
2. **统一多租户隔离** — 采用「单库 + `tenant_id` 行级隔离」（ADR-002/008），由 `TenantContext` 贯穿、`JPA @TenantId` 自动落租户、`TenantRoutingDataSource` 支持独立库/读写分离扩展。
3. **分层管理面** — 管理面（租户/平台运营）与业务面（租户内业务）分离（ADR-003），支撑"中台管租户、租户管业务"。
4. **统一身份与权限中台** — RBAC（用户-角色-权限）+ ABAC（属性条件）、行级数据权限（DataRule）、列级脱敏（ResponseBodyAdvice）、跨租户授权令牌（归属权与参与权分离）。
5. **事件驱动解耦** — `DomainEventPublisher` 进程内 / MQ 双实现，模块间通过领域事件解耦（如 `TenantCreatedEvent` 触发租户数据初始化）。
6. **流程中心** — 内嵌 **Flowable** 共享引擎（ADR-005/006），支持跨租户流程（归属权与参与权分离）。
7. **轻量自研鉴权** — JWT + `HandlerInterceptor`（`TenantInterceptor`）的轻量鉴权链，不强制绑定 Spring Security，便于按中台诉求定制。
8. **全 POST API 风格 + 后端驱动菜单** — API 统一 POST（ADR-010），菜单由后端定义下发（07-menu-specification），前后端职责清晰。
9. **前端工程化** — React 18 + TypeScript + Vite + React Query + Tailwind，自研 `ui` 组件库，提供 `<Can permission="...">` 按钮级权限控制。

---

## 二、技术栈

| 层 | 技术 |
|----|------|
| 后端 | Java 17 · Spring Boot 3.x · Spring Data JPA / Hibernate · MapStruct |
| 数据库 | PostgreSQL（默认）/ MySQL（profile）；开发期 H2；Flyway 迁移 |
| 中间件/引擎 | Flowable（BPM）· JWT（jjwt）· spring-security-crypto（BCrypt） |
| 前端 | React 18 · TypeScript · Vite · React Router · React Query · Tailwind · 自研 `ui` |
| 构建 | Maven（reactor 多模块）· Node / pnpm |

---

## 三、架构概览

### 分层
- **xcms-shared-kernel（共享内核）**：`ApiResponse`、分页、`BaseEntity/TenantEntity`、`TenantContext`、`TenantRoutingDataSource`、领域事件发布器、统一异常体系。所有业务模块依赖它。
- **业务模块（api + impl）**：identity / authorization / organization / tenant-management / workflow / message / file / config / task-scheduling / audit / operation。
- **xcms-portal**：Web 层（租户拦截、路由、CORS）。
- **xcms-app**：应用装配入口（数据源、Flyable、种子数据、安全配置）。

### 模块清单（api/impl 拆分）
| 模块 | 职责 |
|------|------|
| identity | 用户、认证、SSO、会话、租户初始化 |
| authorization | RBAC/ABAC、数据权限、跨租户授权 |
| organization | 部门、岗位、人员 |
| tenant-management | 级联租户、配额、生命周期 |
| workflow | 流程引擎（Flowable） |
| message | 消息中心（站内信） |
| file | 文件存储 |
| config | 配置中心 |
| task-scheduling | 任务调度 |
| audit | 审计中心 |
| operation | 运营管理 / 计量 |

---

## 四、目录结构

```
xctec-xcms/
├── docs/                  # 设计文档站（概述/架构/核心模块/接口/菜单/UI/路线图/ADR）
│   └── reviews/           # 阶段复盘 / 里程碑评审记录
├── db/                    # DDL / 初始化 SQL
├── xcms-backend/          # 后端（Maven 多模块）
│   ├── xcms-shared-kernel/
│   ├── xcms-identity/ ... # 各业务模块（api + impl）
│   ├── xcms-portal/
│   └── xcms-app/
└── xcms-front/            # 前端（React + Vite）
```

---

## 五、快速开始

### 后端
```bash
cd xcms-backend
mvn -pl xcms-app -am spring-boot:run        # 默认 h2 profile，自动种子数据
# 生产：mvn package && java -jar xcms-app/target/xcms-app.jar
```
> 配置见 `xcms-app/src/main/resources/application.yml`；JWT 密钥请通过环境变量 `XCMS_JWT_SECRET` 注入。

### 前端（pnpm 管理）
```bash
cd xcms-front
pnpm install
pnpm dev
```

---

## 六、设计决策（ADR 精选）

完整 10 条见 `docs/README.md`。关键决策：

- **ADR-001** 模块化单体而非微服务 · **ADR-002** 单库 + `tenant_id` 多租户 · **ADR-004** API/Impl 模块拆分
- **ADR-005/006** Flowable 共享引擎 + 跨租户流程（归属权/参与权分离）
- **ADR-007** 独立部署不互通业务数据 · **ADR-008** JPA `@TenantId` 多租户 · **ADR-010** 全 POST API 风格

---

## 七、建设计划与进度（按建设周期）

> 项目按四个阶段（M1–M4）推进，路线图见 `docs/09-roadmap.md`。`[x]` 已落地，`[ ]` 规划/进行中；括号内为对应复盘条目（`docs/reviews/`）。

```
M1 核心基础层 ──▶ M2 流程+共享服务 ──▶ M3 运营+完善 ──▶ M4 独立部署
   Phase 1            Phase 2            Phase 3           Phase 4
```

### ▶ Phase 1：核心基础层（MVP / 里程碑 M1）
*目标：可运行的单体应用；租户/组织/用户/权限完整可操作；管理面 + 业务面基础界面。*

- [x] 模块化单体 + API/Impl 契约/实现分离
- [x] 共享内核（ApiResponse、异常体系、TenantContext、事件发布器）
- [x] 多租户隔离（单库 + `@TenantId` 行级 + TenantContext/RoutingDataSource）
- [x] JPA 映射（MapStruct）、Flyway 数据库迁移
- [x] 租户管理（级联/配额/生命周期）+ 事件驱动初始化（默认角色/管理员/根部门）
- [x] 组织架构（部门树/岗位/人员）
- [x] 身份（用户/本地登录/会话）+ 轻量鉴权链（JWT + `TenantInterceptor`）
- [x] 权限 RBAC + 行级数据权限（DataRule）+ 列级脱敏
- [x] 前端打通：登录 / 用户 / 租户 / 权限 / 组织
- [ ] 全局异常处理 `GlobalExceptionHandler`（H1）
- [ ] `ApiResponse` 401 契约统一（H2）

### ▶ Phase 2：流程中心 + 共享服务（里程碑 M2）
*目标：完整流程中心（含跨租户）+ 消息/配置/文件/审计服务 + 业务面工作台。*

- [x] 流程引擎 Flowable 落地（部署/流程实例/任务）
- [x] 消息中心：站内信入库 + 附件 + 收件人
- [x] 配置中心（参数/功能开关/字典）
- [x] 文件本地存储（上传/下载/预览）
- [x] 审计框架（AOP `@AuditLog` + `AuditEvent` + 监听器 + 查询接口）
- [ ] **流程设计器（BPMN 拖拽建模）**
- [ ] **消息真实投递通道（短信/邮件/微信/钉钉）**
- [ ] **文件对象存储（MinIO/S3/OSS）、断点续传**
- [ ] **审计业务埋点接入（`@AuditLog` 标注关键写操作）+ tenantId/操作者修正（S6）**
- [ ] 前端打通：流程 / 消息 / 文件 / 审计

### ▶ Phase 3：运营管理 + 完善优化（里程碑 M3）
*目标：运营看板 + 任务调度 + SSO + 完整安全审计。*

- [x] 任务调度引擎（自研 TaskSchedulerEngine + 任务锁）
- [x] 运营指标采集（OperationScheduler）
- [x] SSO：OAuth2 / OIDC 鉴权分支
- [x] 跨租户授权令牌（业务可见授权 CrossTenantAuth）
- [ ] **运营看板前端打通真实 API**
- [ ] **计量能力（metering：API 调用量/用量统计）**
- [ ] **SSO 补全：SAML、微信/钉钉/飞书原生协议**
- [ ] **前端实时推送通道（WebSocket/SSE）**
- [ ] 生产安全收尾：JWT 密钥启动强校验（C1）、CORS 收敛（C2）、初始密码随机化（S3）、租户初始化 `@TenantId` 串租户修复（S1）

### ▶ Phase 4：独立部署 + 扩展预留（里程碑 M4）
*目标：支持子公司独立部署并注册到集团管理面，预留扩展接口。*

- [ ] 独立部署模式开关（shared / dedicated 切换）
- [ ] 元数据注册（独立部署租户对接集团管理面）
- [ ] 流程桥接预留（BridgeNode 接口）
- [ ] 开发者门户（API 文档/应用管理）
- [ ] 第三方登录（钉钉/企微/飞书）+ MFA / 二次验证（TOTP）
- [ ] 租户迁移（导入/导出）、跨租户用户切换（impersonate）

---

## 八、文档与复盘

- **设计文档**：`docs/`（概述、架构、核心模块、接口规范、菜单规范、UI 规范、路线图、ADR）
- **阶段复盘**：`docs/reviews/`（问题分级 C/H/M/L、功能缺口 F、补充发现 S，含行动项优先级）

---

## 九、说明

本项目尚在建设中，能力清单与路线图以 `docs/09-roadmap.md` 为准。欢迎基于 `docs/reviews/` 中的复盘结论参与共建。
