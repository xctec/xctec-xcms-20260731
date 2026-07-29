# 系统初始化种子数据规范

> Status: Draft
> Date: 2026-07-29
> 关联：ADR-015（租户初始化事件契约）、docs/10-architecture-adjustment-tasks.md（AT-08/14）、源码 `xcms-app/.../config/SeedDataService.java` / `SeedDataRunner.java`、`xcms-identity-impl/.../listener/TenantUserInitializer.java`、`xcms-org-impl/.../listener/TenantOrgInitializer.java`
>
> 本文档为**开发基准**，明确系统初始化（首次启动 + 每租户创建）需要落入数据库的数据，作为契约与验收依据。复盘/设计记录（`docs/reviews/`、`docs/code-review/`）仅作参考，不作为基准。

---

## 1. 概述：两条初始化路径

系统初始化存在两条相互独立的数据落地路径，作用对象不同，必须分别维护：

| 路径 | 触发方式 | 作用对象 | 代码入口 | 生效条件 |
|---|---|---|---|---|
| **平台级种子（bootstrap）** | `CommandLineRunner` 顺序调用 | **默认租户**（platform 级，code=`default`） | `SeedDataRunner` → `SeedDataService` | 仅 `h2` profile（首次启动） |
| **每租户初始化（事件驱动）** | `TenantCreatedEvent` 经 `DomainEventDispatcher`（`AFTER_COMMIT` + `REQUIRES_NEW`）分发 | 每一个**新建**业务租户（非默认） | `TenantUserInitializer`、`TenantOrgInitializer` | 任意租户经 `TenantService.createTenant` 创建 |

关键事实：

- **默认租户不走事件路径**。`SeedDataService` 直接落库默认租户及其管理员，**不发布 `TenantCreatedEvent`**，因此事件监听器不会为默认租户运行。
- 两条路径**职责不一致**（见第 5 节），是数据缺口与一致性的主要来源。
- 全程**幂等**：所有写入均以唯一键（`tenantCode` / `permCode` / `roleCode` / `(tenantId,userId,roleId)` 等）查重后写入，重复启动/重发事件不会重复或覆盖。

---

## 2. 平台级种子数据（默认租户）

入口：`SeedDataService.ensureDefaultTenant()`（建租户）+ `SeedDataService.seedAdmin(tenantId)`（建管理员/角色/授权），两方法各自独立事务。`SeedDataRunner` 先建租户拿到 `tenantId`，再 `TenantContext.set(tenantId)` 后调用 `seedAdmin`，使事务开启时 Hibernate 会话捕获正确租户，规避 `@TenantId` 会话时序校验。

### 2.1 数据清单

| 数据类别 | 表 | 关键字段 | 幂等键 | 说明 |
|---|---|---|---|---|
| 默认租户 | `tenant_info` | `tenantCode="default"`, `tenantName="默认租户"`, `tenantType=PLATFORM`, `level=0`, `path="/{id}/"`, `status=ACTIVE` | `tenantCode` | 系统级表，无 `@TenantId` 隔离 |
| 管理员账号 | `identity_user` | `username="admin"`, `realName="系统管理员"`, `userType="ADMIN"`, `status=ACTIVE`, `password`(加密), `passwordChangedAt=now` | `username` | ⚠️ 见第 5.2 节密码硬编码问题 |
| 管理员角色 | `identity_role` | `roleCode="tenant_admin"`, `roleName="租户管理员"`, `roleType="SYSTEM"`, `roleScope=TENANT`, `status=ACTIVE` | `roleCode` | ⚠️ `roleType="SYSTEM"` 与事件中 `RoleScope.TENANT` 不一致（第 5.3 节） |
| 用户-角色绑定 | `identity_user_role` | `userId`=admin, `roleId`=tenant_admin, `scopeType="TENANT"`, `scopeValue={tenantId}` | `(userId, roleId)` | |
| 操作权限项 | `perm_operation` | 见第 4 节 19 条 | `permCode` | 平台级，无租户隔离 |
| 角色-权限绑定 | `perm_role_permission` | `tenant_id`, `role_id`=tenant_admin, `perm_type="OPERATION"`, `perm_id` | `(tenant_id, role_id, perm_type, perm_id)` | 租户级 |

> 默认租户**仅建 `tenant_admin` 角色**，不建 `tenant_user` 角色、不建根部门（第 5.1 节缺口）。

---

## 3. 每租户初始化数据（事件驱动）

入口：`TenantCreatedEvent` 监听器。`tenant_init_status` 表按 `(tenantId, module)` 记录成功/失败，失败不回滚租户创建，可由 `TenantInitStatusServiceImpl.retry` 重发事件补偿。

### 3.1 身份模块 — `TenantUserInitializer`（MODULE_IDENTITY）

| 数据类别 | 表 | 关键字段 | 幂等键 |
|---|---|---|---|
| 管理员角色 | `identity_role` | `roleCode="tenant_admin"`, `roleName="租户管理员"`, `roleType=TENANT`, `roleScope=TENANT` | `tenantId + roleCode` |
| 普通用户角色 | `identity_role` | `roleCode="tenant_user"`, `roleName="普通用户"`, `roleType=TENANT`, `roleScope=TENANT` | `tenantId + roleCode` |
| 管理员账号 | `identity_user` | `username="admin"`, `realName="系统管理员"`, `status=ACTIVE`, **`password`=随机 12 位**（`AT-15`），`passwordChangedAt=null`（首登强制改密） | `tenantId + username` |
| 用户-角色绑定 | `identity_user_role` | admin ↔ tenant_admin | `tenantId + userId + roleId` |

> 与平台种子区别：密码**随机生成且首登改密**（`AT-15` 安全收敛）；角色 `roleType=TENANT`。

### 3.2 组织模块 — `TenantOrgInitializer`（MODULE_ORG）

| 数据类别 | 表 | 关键字段 | 幂等键 |
|---|---|---|---|
| 根部门 | `org_department` | `deptCode="ROOT"`, `deptName=租户名`, `parentId=null`, `level=1`, `sortOrder=0`, `status="ACTIVE"`, `path="/{id}"` | `tenantId + parentId is null` 查询 |

> ADR-015 契约另列「默认岗位」，但 `TenantOrgInitializer` 仅建根部门，**未建默认岗位**（第 5 节缺口）。

---

## 4. 操作权限码清单（平台级种子，`perm_operation`）

`SeedDataService.OPERATION_PERMISSIONS` 的 19 条，与前端权限码及后端 `@PreAuthorize("hasAuthority('xxx')")` 对齐。声明式鉴权的权限码来源于 `perm_operation.perm_code`（经角色-权限绑定装载），不补齐则 `@PreAuthorize` 对所有人恒返回 403。

| # | perm_code | perm_name | module | resource_type | action |
|---|---|---|---|---|---|
| 1 | `tenant:create` | 新建租户 | 租户管理 | TENANT | create |
| 2 | `tenant:edit` | 编辑租户 | 租户管理 | TENANT | update |
| 3 | `tenant:delete` | 删除租户 | 租户管理 | TENANT | delete |
| 4 | `org:dept:create` | 新建部门 | 组织架构 | DEPARTMENT | create |
| 5 | `org:position:create` | 新建岗位 | 组织架构 | POSITION | create |
| 6 | `org:group:create` | 新建用户组 | 组织架构 | USER_GROUP | create |
| 7 | `org:edit` | 编辑组织 | 组织架构 | ORG | update |
| 8 | `user:create` | 新建用户 | 用户管理 | USER | create |
| 9 | `user:edit` | 编辑用户 | 用户管理 | USER | update |
| 10 | `user:delete` | 删除用户 | 用户管理 | USER | delete |
| 11 | `user:export` | 导出用户 | 用户管理 | USER | read |
| 12 | `user:reset-pwd` | 重置密码 | 用户管理 | USER | update |
| 13 | `role:create` | 新建角色 | 权限管理 | ROLE | create |
| 14 | `role:edit` | 编辑角色 | 权限管理 | ROLE | update |
| 15 | `role:delete` | 删除角色 | 权限管理 | ROLE | delete |
| 16 | `role:assign` | 分配角色 | 权限管理 | ROLE | update |
| 17 | `role:permission` | 分配权限 | 权限管理 | ROLE | update |
| 18 | `role:data-scope` | 数据范围 | 权限管理 | ROLE | update |
| 19 | `workflow:deploy` | 流程部署 | 流程管理 | WORKFLOW | update |

**对齐核对（已验证）**：生产代码 `hasAuthority(...)` 实际使用的码（`org:*`、`user:*`、`role:*`、`tenant:create/edit`、`workflow:deploy` 等）**全部被以上 19 条覆盖**。`ROLE_SERVICE` 不在此列——它由服务令牌（`AT-11`）经 `JwtPermissionAuthenticationConverter` 直接授予，不属租户权限，无需入种子。种子中 `tenant:delete` / `user:export` / `role:permission` 为前瞻性补充（admin 可将其授予其他角色）。

---

## 5. 缺口与一致性问题

### 5.1 数据缺口（ADR-015 契约列了但代码未实现）

| 契约项（ADR-015 §1） | 状态 | 影响 |
|---|---|---|
| 默认菜单分配（`perm_menu` 种子 + 角色菜单绑定） | ❌ `perm_menu` 无任何种子；无菜单监听 | 前端 `/admin/permission/menus`、`/portal/menus` 返回空，菜单树不可用 |
| 数据权限规则/脱敏默认（`perm_data_rule`、`perm_column_mask`、`perm_data_rule_role`） | ❌ 未种子 | 数据权限（AT-16/18）无默认规则可用 |
| 默认岗位（`Position`） | ❌ `TenantOrgInitializer` 只建根部门 | 契约列了「默认岗位」，未落地 |
| 默认消息模板/系统参数（message/config） | ❌ 无监听 | 契约列了，未实现 |
| 默认租户的 `tenant_user` 角色 / 根部门 | ⚠️ 默认租户只走种子路径，缺这二项 | 默认租户无普通用户角色、无根部门 |

### 5.2 安全一致性（建议修复）

- **种子 admin 密码硬编码 `admin123`**（`SeedDataService.ADMIN_PASSWORD`），且 `passwordChangedAt` 已设为 `now`（→ 不强制改密）。与 ADR-015/AT-15「随机密码 + 首登强制改密」直接冲突。建议改为随机生成并经安全渠道下发（或复用 `TenantUserInitializer` 逻辑）。

### 5.3 路径/模型不一致（建议收敛）

- **默认租户缺 `tenant_user` 角色与根部门**：路径分裂导致默认租户数据不全。建议默认租户也经事件路径统一初始化，或种子补齐。
- **角色 `roleType` 取值不一致**：平台种子用字符串 `"SYSTEM"`，事件路径用 `RoleScope.TENANT` 枚举值 `"TENANT"`。建议统一为枚举值，避免后续按 `roleType` 查询/分支出错。
- **`userType="ADMIN"` 仅种子写入**：事件路径 admin 不写 `userType`，模型语义不统一。

---

## 6. 验收与维护约定

1. 新增 `@PreAuthorize("hasAuthority('x:y')")` 时，**必须同步在 `OPERATION_PERMISSIONS` 增补对应权限码**，否则该接口对租户管理员恒 403。
2. 所有种子写入必须带幂等键（唯一约束/显式带 `tenantId` 查询），保证可重复执行与补偿重放。
3. 新增业务模块的「租户初始化职责」须按 ADR-015 §1 契约在对应 `*Initializer` 中落地，并在本文档补充数据清单，不得散落于 `SeedDataService`。
4. 安全类数据（密码、密钥）不得硬编码明文落库或日志；密码经安全渠道下发。

---

> 本文档为开发基准。缺口项（第 5.1 节菜单种子、数据权限默认、默认岗位）建议作为后续任务补入 `docs/10-architecture-adjustment-tasks.md`；安全一致性问题（第 5.2 节）建议优先修复。
