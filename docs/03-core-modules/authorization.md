# 4 权限中心\n
### 3.4 权限中心

#### 3.4.1 功能概述

权限中心是 XCMS 最核心的模块，提供 RBAC + ABAC 混合权限模型，支持复杂的行级数据权限、列级数据权限、自定义数据范围，以及跨租户业务可见授权。

#### 3.4.2 权限模型总览

```
┌─────────────────────────────────────────────┐
│              权限模型总览                      │
│                                             │
│  ┌─────────────┐    ┌─────────────┐         │
│  │   RBAC      │    │   ABAC      │         │
│  │ 角色权限控制  │    │ 属性规则控制  │         │
│  │             │    │             │         │
│  │ 用户→角色    │    │ 主体属性     │         │
│  │ 角色→权限项  │    │ 资源属性     │         │
│  │             │    │ 环境属性     │         │
│  │ 控制能做什么  │    │ 规则→允许/拒绝│        │
│  └──────┬──────┘    └──────┬──────┘         │
│         │                  │                 │
│         └──────┬───────────┘                 │
│                │                             │
│         ┌──────┴──────┐                      │
│         │  数据权限    │                      │
│         │             │                      │
│         │ 行级权限     │ 控制能看哪些行        │
│         │ 列级权限     │ 控制能看哪些字段      │
│         │ 自定义范围   │ 灵活规则              │
│         └─────────────┘                      │
└─────────────────────────────────────────────┘
```

#### 3.4.3 数据模型

```
perm_menu（菜单权限项表）
├── id
├── menu_code           菜单编码
├── menu_name           菜单名称
├── parent_id           父菜单
├── menu_type           类型（CATALOG/MENU/BUTTON）
├── path                路由路径
├── icon                图标
├── sort_order          排序
├── visible             是否可见
├── scope               范围（ADMIN/BUSINESS/BOTH）
└── status

perm_operation（操作权限项表）
├── id
├── perm_code           权限编码（如 order:create）
├── perm_name           权限名称
├── module              所属模块
├── resource_type       资源类型
├── action              操作（CREATE/READ/UPDATE/DELETE/EXPORT）
└── description

perm_role_permission（角色权限关联表）
├── id
├── tenant_id           租户ID（@TenantId）
├── role_id             角色ID
├── perm_type           权限类型（MENU/OPERATION）
├── perm_id             权限项ID
└── scope_config        数据权限配置（JSON）

perm_data_rule（数据权限规则表）
├── id
├── tenant_id           租户ID（@TenantId）
├── rule_name           规则名称
├── rule_type           类型（ROW/COLUMN/CUSTOM）
├── resource_type       资源类型（如 Order/Customer）
├── dimension           维度（ORG/BUSINESS_LINE/REGION/TAG/TIME/OWNER）
├── rule_config         规则配置（JSON）
├── priority            优先级
└── status

perm_data_rule_role（数据规则角色关联表）
├── id
├── tenant_id           租户ID（@TenantId）
├── rule_id             规则ID
├── role_id             角色ID
└── scope_value         范围值（如部门ID列表）

perm_column_mask（列级脱敏规则表）
├── id
├── tenant_id           租户ID（@TenantId）
├── resource_type       资源类型
├── field_name          字段名
├── mask_type           脱敏类型（HIDE/MASK/PARTIAL）
├── mask_rule           脱敏规则（如 138****1234）
├── role_ids            适用角色（JSON数组）
└── status

perm_cross_tenant_auth（跨租户授权表）
├── id
├── tenant_id           发起租户ID
├── target_tenant_id    目标租户ID
├── user_id             授权用户
├── data_scope          数据范围（JSON）
├── token               授权令牌
├── valid_from          生效时间
├── valid_until         过期时间
├── status              状态（PENDING/APPROVED/ACTIVE/EXPIRED/REVOKED）
├── approved_by         审批人
├── approved_at         审批时间
├── reason              授权原因
└── created_at
```

#### 3.4.4 RBAC 权限控制

**权限项分类**：

| 类型 | 说明 | 示例 |
|------|------|------|
| 菜单权限 | 控制用户可见的菜单 | 租户管理菜单、流程中心菜单 |
| 操作权限 | 控制用户可执行的操作 | order:create, order:read, order:export |

**角色继承**：
- 角色可继承父角色的权限
- 子角色权限 ⊇ 父角色权限
- 用于简化权限分配（如"管理员"继承"普通用户"权限）

**权限分配范围**：
- 租户级：在整个租户范围内有效
- 部门级：仅在指定部门范围内有效
- 自定义：自定义数据范围

#### 3.4.5 数据权限控制

**行级权限维度**：

| 维度 | 字段 | 规则示例 |
|------|------|---------|
| 按组织 | dept_path | `dept_path LIKE '/1/10/%'`（本部门及下级） |
| 按业务线 | business_line | `business_line IN (SELECT ...)` |
| 按地域 | region | `region = '华东'` |
| 按自定义标签 | tags | `tags @> '["VIP"]'`（JSON数组包含） |
| 按时间范围 | created_at | `created_at >= NOW() - INTERVAL '3 months'` |
| 按数据归属 | owner_id | `owner_id = :currentUserId` |

**多维度组合**：使用 JPA `Specification` 动态组合：

```java
public class DataPermissionSpec {
    
    public static <T extends TenantEntity> Specification<T> build(
            DataPermissionContext ctx) {
        Specification<T> spec = Specification.where(
            tenantScope(ctx.getTenantId())           // 租户隔离
        );
        
        if (ctx.hasOrgScope()) {
            spec = spec.and(orgScope(ctx.getOrgPath()));   // 组织范围
        }
        if (ctx.hasBusinessLineScope()) {
            spec = spec.and(businessLineScope(
                ctx.getBusinessLineIds()));                // 业务线范围
        }
        if (ctx.hasRegionScope()) {
            spec = spec.and(regionScope(ctx.getRegions()));// 地域范围
        }
        if (ctx.hasTagScope()) {
            spec = spec.and(tagScope(ctx.getTags()));      // 标签范围
        }
        if (ctx.hasTimeScope()) {
            spec = spec.and(timeScope(
                ctx.getTimeFrom(), ctx.getTimeTo()));      // 时间范围
        }
        if (ctx.hasOwnerScope()) {
            spec = spec.and(ownerScope(ctx.getUserId()));  // 数据归属
        }
        
        return spec;
    }
}
```

**列级权限**：

| 类型 | 说明 | 示例 |
|------|------|------|
| 字段隐藏 | 指定字段不返回 | 薪酬字段对非HR角色隐藏 |
| 字段脱敏 | 返回但脱敏 | 手机号 138****1234 |
| 部分可见 | 返回部分内容 | 身份证仅显示前4后4 |

```java
// 列级权限在数据返回时过滤
public class ColumnMaskInterceptor {
    
    public <T> T mask(T entity, Long userId) {
        List<ColumnMaskRule> rules = maskService
            .getRules(entity.getClass(), userId);
        
        for (ColumnMaskRule rule : rules) {
            Field field = entity.getClass()
                .getDeclaredField(rule.getFieldName());
            field.setAccessible(true);
            Object value = field.get(entity);
            
            switch (rule.getMaskType()) {
                case HIDE:
                    field.set(entity, null);           // 隐藏
                    break;
                case MASK:
                    field.set(entity, maskValue(
                        value, rule.getMaskRule()));    // 脱敏
                    break;
                case PARTIAL:
                    field.set(entity, partialValue(
                        value, rule.getMaskRule()));    // 部分可见
                    break;
            }
        }
        return entity;
    }
}
```

**自定义数据范围**：
- 管理员可定义自定义规则（基于 ABAC）
- 规则由：主体属性 + 资源属性 + 操作符 + 值 组成
- 示例：`用户.职级 >= 资源.可见职级 AND 用户.部门 IN 资源.可见部门`

#### 3.4.6 跨租户业务可见授权

```
授权流程：
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ 发起授权  │───→│ 审批流程  │───→│ 生成令牌  │───→│ 使用授权  │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
    │               │               │               │
    │ 管理员        │ 审批人        │ 系统自动      │ 管理员
    │ 选择：        │ 审批：        │ 生成：        │ 查询：
    │ -目标租户     │ 通过/拒绝     │ -授权令牌     │ -带令牌请求
    │ -数据范围     │               │ -有效期       │ -自动过滤
    │ -有效期       │               │ -审计记录     │ -记录审计
    │ -原因         │               │               │
└──────────────────────────────────────────────────────┘
                           │
                           ▼
                      到期自动回收
```

**令牌机制**：
- 令牌包含：目标租户ID、数据范围、有效期、签发人
- 令牌绑定用户，不可转让
- 令牌有有效期，到期自动失效
- 管理员可手动撤销令牌
- 所有令牌使用记录入审计

---

