# Module List

> XCMS 模块清单

| 模块 | 说明 |
|------|------|
| shared-kernel | 共享内核（租户上下文、数据源路由、通用工具） |
| tenant-management | 租户管理（级联租户、生命周期、配额） |
| organization | 组织架构管理 |
| identity | 统一身份与认证（SSO、用户、登录） |
| authorization | 权限中心（RBAC+ABAC、数据权限、业务可见授权） |
| workflow | 流程中心（Flowable BPM、审批、跨租户流程） |
| message | 消息中心（站内信、通知、多渠道） |
| configuration | 配置中心（动态配置、功能开关、字典） |
| file-storage | 文件存储中心 |
| task-scheduling | 任务调度中心 |
| audit | 审计日志中心 |
| portal | 门户（统一入口、工作台、API层） |
| operation | 运营管理（看板、计量、监控） |
| app | 启动模块（组装所有模块、主配置） |

## Module Structure (api/impl split)

每个业务模块拆分为 api 和 impl 两个子模块：

```
xcms/
├── shared-kernel/
├── tenant-management/
│   ├── tenant-api/
│   └── tenant-impl/
├── organization/
│   ├── org-api/
│   └── org-impl/
├── identity/
│   ├── identity-api/
│   └── identity-impl/
├── authorization/
│   ├── auth-api/
│   └── auth-impl/
├── workflow/
│   ├── workflow-api/
│   └── workflow-impl/
├── message/
│   ├── message-api/
│   └── message-impl/
├── configuration/
│   ├── config-api/
│   └── config-impl/
├── file-storage/
│   ├── file-api/
│   └── file-impl/
├── task-scheduling/
│   ├── task-api/
│   └── task-impl/
├── audit/
│   ├── audit-api/
│   └── audit-impl/
├── portal/
├── operation/
└── app/
```

## Key Design Decisions

| 决策 | 选择 | 理由 |
|------|------|------|
| 架构形态 | 单体多模块 | 0→1阶段简单高效，独立部署友好，可演进 |
| 多租户隔离 | 单库 + tenant_id | 起步成本低，预留数据源路由可演进 |
| 租户层级 | 无限层级设计(≤10级现实) | 满足复杂集团架构 |
| 管理面架构 | 分层管理面(方案C) | 匹配集团层级管理，权责对齐 |
| 管理可见/业务可见 | 默认管理可见，业务可见需授权 | 保障数据安全与租户自治 |
| 独立部署互通 | 不互通，走外部接口/预留扩展 | 数据安全隔离 |
| 流程引擎 | Flowable共享引擎 | 跨租户流程在共享引擎内更易实现 |
| 流程跨租户 | 归属权与参与权分离 | 流程数据不迁移，任务跨租户分配 |
| ORM | JPA (Hibernate 7) | @TenantId 原生多租户支持 |
| 查询构建 | QueryDSL | 类型安全查询，与 @TenantId 兼容 |
| 对象映射 | MapStruct | 编译期生成，零反射，Entity ↔ DTO 转换 |
| 工具 | Lombok | 简化样板代码 |
| 数据库 | 数据库无关 | JPA抽象，兼容多种数据库 |
| API网关 | 不使用(单体无需) | 模块间方法调用，未来可加 |

> 详细决策记录见 [adr/](../adr/) 目录
