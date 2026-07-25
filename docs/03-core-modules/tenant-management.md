# 1 租户管理\n
### 3.1 租户管理

#### 3.1.1 功能概述

租户管理是 XCMS 的基石，提供级联租户的全生命周期管理，包括租户创建、层级管理、配额管控、类型分类、生命周期管理。

#### 3.1.2 数据模型

```
tenant_info（租户信息表，系统级表，无 tenant_id）
├── id                  租户ID
├── tenant_code         租户编码（唯一）
├── tenant_name         租户名称
├── tenant_type         租户类型（ORGANIZATION/PROJECT/EXTERNAL）
├── parent_id           父租户ID（根租户为null）
├── level               层级（0=根, 1=一级, ...）
├── path                层级路径（如 /1/10/101/，便于查询子树）
├── status              状态（ACTIVE/SUSPENDED/LOCKED/MIGRATING）
├── deployment_mode     部署模式（SHARED/DEDICATED）
├── datasource_key      数据源标识（预留，默认shared）
├── created_by          创建人
├── created_at          创建时间
├── updated_at          更新时间
└── deleted_at          软删除时间

tenant_config（租户配置表）
├── id
├── tenant_id           租户ID
├── config_key          配置键
├── config_value        配置值
└── config_type         配置类型

tenant_quota（租户配额表）
├── id
├── tenant_id           租户ID
├── quota_type          配额类型（USER_COUNT/STORAGE/API_CALL/PROCESS_INSTANCE）
├── quota_limit         配额上限
├── quota_used          已用量（定时更新）
├── allocated_to        已分配给下级的量
└── period              周期（DAILY/MONTHLY/TOTAL）

tenant_feature（租户功能开关表）
├── id
├── tenant_id           租户ID
├── feature_code        功能编码
├── enabled             是否启用
└── config              功能配置（JSON）

tenant_relation（租户关系表，项目型租户用）
├── id
├── project_tenant_id   项目租户ID
├── member_tenant_id    参与方租户ID
├── member_org_id       参与方组织ID（可选，精确到部门）
├── role                参与角色
└── status              状态
```

#### 3.1.3 核心功能

**租户树管理**：
- 树形展示租户层级，支持搜索/筛选
- 通过 `path` 字段高效查询子树（`WHERE path LIKE '/1/10/%'`）
- 支持拖拽调整层级（变更 parent_id 和 path）

**租户创建**：
- 选择租户类型（组织型/项目型/外部合作方）
- 选择父租户（自动设置 level 和 path）
- 分配配额（从父租户配额中分配）
- 设置功能开关
- 创建完成后自动初始化租户的管理面和业务面

**租户生命周期**：

```
              创建
               │
               ▼
          ┌────────┐
          │ ACTIVE │ ←────────┐
          └───┬────┘          │
     ┌────────┼────────┐      │ 解锁
     ▼        ▼        ▼      │
┌────────┐┌────────┐┌────────┐│
│SUSPENDED││ LOCKED ││MIGRATING││
│(停用)   ││(锁定)  ││(迁移中) ││
└───┬────┘└───┬────┘└───┬────┘│
    │ 启用      │ 解锁     │ 完成│
    └──────────┴─────────┴─────┘
                    │
                    ▼
              ┌──────────┐
              │ ARCHIVED │  归档（软删除）
              └──────────┘
```

**租户迁移**：
- 将租户从一个父租户迁移到另一个
- 迁移过程：锁定 → 迁移数据归属 → 更新层级路径 → 解锁
- 迁移期间该租户及子树只读

**配额管理**：
- 上级为下级分配配额，下级可再分配
- 配额检查在用户创建、文件上传、API调用时触发
- 配额预警：用量达80%时通知租户管理员

#### 3.1.4 独立部署租户管理

独立部署的租户在集团管理面中仅保留元数据注册：

```
集团管理面看到的独立部署租户：
├── 租户基本信息（名称、编码、类型）
├── 部署地址（独立实例的访问URL）
├── 部署模式：DEDICATED
├── 资源用量（由独立实例上报）
└── 不包含：业务数据、用户明细、流程实例
```

---

