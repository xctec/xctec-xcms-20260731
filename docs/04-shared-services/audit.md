# 5 审计日志中心

### 4.5 审计日志中心

#### 4.5.1 功能概述

提供全面的审计日志记录，包括管理操作审计、跨租户访问审计、登录审计、数据访问审计。所有审计日志不可篡改。

#### 4.5.2 数据模型

```
audit_log（审计日志表）
├── id
├── tenant_id            租户ID（操作发生的租户）
├── audit_type           类型（MANAGEMENT/CROSS_TENANT/LOGIN/DATA_ACCESS/BUSINESS_VISIBLE）
├── user_id              操作人ID
├── user_name            操作人名称
├── user_tenant_id       操作人所属租户
├── target_tenant_id     目标租户（跨租户时）
├── module               模块
├── action               操作（CREATE/UPDATE/DELETE/READ/EXPORT/LOGIN/LOGOUT）
├── resource_type        资源类型
├── resource_id          资源ID
├── description          操作描述
├── request_url          请求URL
├── request_method       HTTP方法
├── request_params       请求参数（脱敏后）
├── response_status      响应状态
├── ip                   操作IP
├── user_agent           User-Agent
├── token_id             授权令牌ID（业务可见授权时）
├── created_at           操作时间
└── extra                扩展信息（JSON）

audit_policy（审计策略表，系统级）
├── id
├── policy_name          策略名称
├── audit_type           审计类型
├── modules              审计模块范围（JSON数组）
├── actions              审计操作范围（JSON数组）
├── retention_days       保留天数
└── enabled
```

#### 4.5.3 审计类型

| 类型 | 说明 | 记录内容 |
|------|------|---------|
| 管理操作审计 | 管理面的所有操作 | 租户/用户/权限/配置的增删改 |
| 跨租户访问审计 | 跨租户数据访问 | 访问人、源租户、目标租户、访问资源 |
| 登录审计 | 登录相关 | 登录成功/失败、IP、设备 |
| 数据访问审计 | 敏感数据访问 | 谁在什么时候访问了什么敏感数据 |
| 业务可见审计 | 业务可见授权使用 | 令牌使用记录、查看的业务数据 |

#### 4.5.4 审计特性

- **不可篡改**：审计日志只增不删，禁止修改
- **异步记录**：通过异步方式记录，不影响业务性能
- **可导出**：支持按条件筛选导出
- **保留策略**：按审计类型配置保留天数，超期归档
- **全文搜索**：支持按操作人、时间、模块、操作类型搜索

---

