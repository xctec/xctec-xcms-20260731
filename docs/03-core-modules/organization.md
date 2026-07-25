# 2 组织架构管理\n
### 3.2 组织架构管理

#### 3.2.1 功能概述

每个租户内维护独立的组织架构，支持部门树、岗位、人员关系管理。组织架构是数据权限（按组织）的基础。

#### 3.2.2 数据模型

```
org_department（部门表）
├── id
├── tenant_id           租户ID（@TenantId）
├── dept_code           部门编码
├── dept_name           部门名称
├── parent_id           父部门ID
├── level               层级
├── path                层级路径（/1/10/100/）
├── manager_id          部门负责人
├── sort_order          排序
├── status              状态
├── created_at
└── updated_at

org_position（岗位表）
├── id
├── tenant_id           租户ID（@TenantId）
├── dept_id             所属部门
├── position_code       岗位编码
├── position_name       岗位名称
├── level               岗位级别
├── sort_order
└── status

org_user_position（用户岗位关联表）
├── id
├── tenant_id           租户ID（@TenantId）
├── user_id             用户ID
├── dept_id             部门ID
├── position_id         岗位ID
├── is_primary          是否主岗位
└── status

org_user_group（用户组表，跨部门）
├── id
├── tenant_id           租户ID（@TenantId）
├── group_name          用户组名称
├── description
└── type                类型

org_user_group_member（用户组成员表）
├── id
├── tenant_id           租户ID（@TenantId）
├── group_id            用户组ID
└── user_id             用户ID
```

#### 3.2.3 核心功能

- **部门树管理**：增删改查、拖拽排序、层级调整
- **岗位管理**：岗位定义、岗位与人员关联
- **人员分配**：用户分配到部门/岗位，支持一人多岗
- **用户组**：跨部门的用户分组，便于权限分配
- **组织导入**：Excel 批量导入组织架构

#### 3.2.4 组织架构与权限的关系

组织架构的 `path` 字段用于数据权限的"按组织"范围控制：

```
数据权限示例：用户属于部门 path=/1/10/100/
├── 本部门：WHERE dept_path = '/1/10/100/'
├── 本部门及下级：WHERE dept_path LIKE '/1/10/100/%'
├── 本部门及上级：WHERE dept_path IN ('/1/', '/1/10/', '/1/10/100/')
└── 全部：无限制
```

---

