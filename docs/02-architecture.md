# XCMS Architecture

## 2. 总体架构

### 2.1 业务架构（能力地图）

```
┌──────────────────────────────────────────────────────────────┐
│                         XCMS 门户 (Portal)                     │
│                  统一入口 · 工作台 · 开发者门户                  │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─── 运营管理层 ───────────────────────────────────────┐    │
│  │  运营看板  ·  计量管理  ·  监控告警  ·  资源治理        │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌─── 共享业务层 ───────────────────────────────────────┐    │
│  │  消息中心  ·  配置中心  ·  文件存储  ·  任务调度  ·  审计 │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌─── 核心基础层 ───────────────────────────────────────┐    │
│  │  租户管理  ·  组织架构  ·  身份认证  ·  权限中心  ·  流程 │    │
│  │            （级联租户）  （IAM/SSO）  （RBAC+ABAC） （BPM）│    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌─── 共享内核 ─────────────────────────────────────────┐    │
│  │  租户上下文  ·  数据源路由  ·  通用工具  ·  基础实体     │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
├──────────────────────────────────────────────────────────────┤
│                      基础设施（外部）                          │
│           数据库  ·  对象存储  ·  消息队列  ·  缓存             │
└──────────────────────────────────────────────────────────────┘
```

### 2.2 技术架构

#### 2.2.1 整体技术选型

| 层次 | 选型 | 说明 |
|------|------|------|
| 语言 | Java 21+ | LTS 版本 |
| 框架 | Spring Boot 4 | 含 Spring Framework 7 |
| ORM | JPA (Hibernate 7) | 原生多租户支持 `@TenantId` |
| 查询构建 | QueryDSL | 类型安全查询，与 `@TenantId` 兼容，用于复杂查询和数据权限条件组合 |
| 对象映射 | MapStruct | 编译期生成代码，零反射，Entity ↔ DTO 转换 |
| 工具 | Lombok | 简化样板代码，与 MapStruct 配合需配置 processor 顺序 |
| 流程引擎 | Flowable | 内嵌部署，Discriminator 多租户模式 |
| 数据库 | 数据库无关 | 基于 JPA 抽象，兼容 MySQL/PostgreSQL 等 |
| 缓存 | Redis | 会话、热点数据、分布式锁 |
| 消息队列 | 可选（预留接口） | 异步任务、事件通知 |
| 对象存储 | 可选（预留接口） | 文件存储后端，支持本地/MinIO/OSS 等 |
| 构建 | Maven/Gradle | 多模块项目 |
| 部署 | JAR + 虚拟机 | 物理机/虚拟机/容器均可 |

#### 2.2.2 单体多模块架构

采用 **Modular Monolith（模块化单体）** 架构，所有能力集成在一个 Java 应用中，通过 Maven/Gradle 多模块组织。

每个业务模块拆分为 **api（契约）** 和 **impl（实现）** 两个子模块，api 模块定义接口和 DTO，impl 模块提供实现。这是从单体平滑演进到微服务的关键设计。

```
xcms/
├── shared-kernel/              共享内核（租户上下文、数据源路由、通用工具）
│                                 只放全局共享的基础设施，不放业务接口
│
├── tenant-management/
│   ├── tenant-api/             接口 + DTO + 事件（TenantService、TenantDTO、TenantCreatedEvent）
│   └── tenant-impl/            实现（TenantServiceImpl、TenantEntity、TenantRepository）
│
├── organization/
│   ├── org-api/                接口 + DTO（OrgService、DeptDTO）
│   └── org-impl/               实现
│
├── identity/
│   ├── identity-api/           接口 + DTO + 事件（UserService、UserDTO、UserCreatedEvent）
│   └── identity-impl/          实现（UserServiceImpl、UserEntity、UserRepository）
│
├── authorization/
│   ├── auth-api/               接口 + DTO + 事件（PermissionService、DataScopeDTO）
│   └── auth-impl/              实现
│
├── workflow/
│   ├── workflow-api/           接口 + DTO（ProcessService、TaskService、ProcessDTO）
│   └── workflow-impl/          实现（含 Flowable 集成）
│
├── message/
│   ├── message-api/            接口 + DTO（MessageService、MessageDTO）
│   └── message-impl/           实现
│
├── configuration/
│   ├── config-api/             接口 + DTO（ConfigService、DictionaryDTO）
│   └── config-impl/            实现
│
├── file-storage/
│   ├── file-api/               接口 + DTO（FileService、FileDTO）
│   └── file-impl/              实现
│
├── task-scheduling/
│   ├── task-api/               接口 + DTO（TaskService、ScheduleDTO）
│   └── task-impl/              实现
│
├── audit/
│   ├── audit-api/              接口 + DTO（AuditService、AuditLogDTO）
│   └── audit-impl/             实现
│
├── portal/                     门户（统一入口、工作台、API层）
├── operation/                  运营管理（看板、计量、监控）
└── app/                        启动模块（组装所有 impl、主配置）
```

**各层职责划分**：

| 层级 | 内容 | 说明 |
|------|------|------|
| `shared-kernel` | 租户上下文、基础实体、数据源路由、通用工具、通用响应 | 全局共享基础设施，不放任何业务接口 |
| `xxx-api` | Service 接口、DTO、事件类、常量/枚举、模块异常 | 模块契约，无实现、无 JPA 实体、无 Repository |
| `xxx-impl` | Service 实现、JPA Entity、Repository、Controller、Entity↔DTO转换 | 模块实现，只有 `app` 依赖此层 |
| `app` | Spring Boot 启动类、全局配置、组装所有 impl | 唯一的启动入口 |

#### 2.2.3 模块依赖关系

##### 依赖规则

```
核心规则：
├── *-api 模块：可以依赖 shared-kernel 和其他 *-api（最小化）
├── *-impl 模块：依赖自己的 *-api + 其他 *-api + shared-kernel
├── *-impl 模块：绝不依赖其他 *-impl  ← 这条是关键！
└── app 模块：依赖所有 *-impl，负责组装
```

```
依赖关系图：

                    ┌───────┐
                    │  app  │  ← 组装所有 impl
                    └───┬───┘
        ┌───────┬───────┼───────┬───────┐
        ▼       ▼       ▼       ▼       ▼
   ┌────────┐┌──────┐┌────────┐┌─────┐┌─────┐
   │wf-impl ││id-impl││auth-impl││msg-impl││...  │  ← impl 之间不互相依赖
   └───┬────┘└──┬───┘└───┬────┘└──┬──┘└─────┘
       │        │        │        │
   ┌───┴────────┴────────┴────────┴───┐
   │                                    │
   │  各自依赖的 *-api 模块：             │
   │  workflow-impl 依赖：              │
   │    identity-api  (查用户)          │
   │    auth-api      (查权限)          │
   │    message-api   (发通知)          │
   │    tenant-api    (查租户)          │
   │                                    │
   │  但不依赖这些模块的 impl！           │
   └────────────────────────────────────┘
       │        │        │        │
       ▼        ▼        ▼        ▼
   ┌────────┐┌──────┐┌────────┐┌─────┐
   │wf-api  ││id-api││auth-api││msg- ││
   │        ││      ││        ││api  ││  ← api 模块之间尽量不依赖
   └───┬────┘└──┬───┘└───┬────┘└──┬──┘
       │        │        │        │
       └────────┴────────┴────────┘
                    │
              ┌─────┴─────┐
              │shared-kernel│  ← 只依赖这一个
              └───────────┘
```

##### 各层放什么

**shared-kernel（最小化，全局共享）**：

```
shared-kernel/
├── context/
│   └── TenantContext.java          租户上下文（ThreadLocal）
├── entity/
│   └── TenantEntity.java           租户实体基类（@TenantId）
├── datasource/
│   └── TenantRoutingDataSource.java 数据源路由（预留）
├── common/
│   ├── PageResult.java             通用分页结果
│   ├── ApiResponse.java            通用响应包装
│   └── BaseEntity.java             基础实体（id/created_at/updated_at）
├── exception/
│   ├── BusinessException.java      业务异常基类
│   └── PermissionException.java    权限异常
└── util/
    └── DateUtils.java              通用工具
```

**xxx-api（模块契约，轻量）**：

```
identity-api/
├── service/
│   └── UserService.java            接口定义（无实现）
├── dto/
│   ├── UserDTO.java                数据传输对象
│   └── UserCreateRequest.java
├── event/
│   └── UserCreatedEvent.java       事件（其他模块可监听）
├── constant/
│   └── UserStatus.java             常量/枚举
└── exception/
    └── UserNotFoundException.java  模块异常

注意：没有任何 JPA 实体、没有 Repository、没有实现类
```

**xxx-impl（模块实现，只有 app 依赖）**：

```
identity-impl/
├── service/
│   └── UserServiceImpl.java        接口实现
├── entity/
│   └── UserEntity.java             JPA 实体（不对外暴露）
├── repository/
│   └── UserRepository.java         JPA Repository（不对外暴露）
├── controller/
│   └── UserController.java         REST 控制器
├── mapper/
│   └── UserMapper.java             Entity ↔ DTO 转换
├── listener/
│   └── TenantCreatedListener.java  监听其他模块事件
└── config/
    └── IdentityConfig.java         模块配置
```

##### 模块间通信方式

**方式 1：直接接口调用（同步）**

适用于需要立即获取结果的场景。调用方依赖被调方的 api 模块，Spring 在运行时注入实现：

```java
// workflow-impl 调用 identity-api 的接口
@Service
public class ProcessServiceImpl implements ProcessService {
    private final UserService userService;        // identity-api 接口
    
    public ProcessServiceImpl(UserService userService) {
        this.userService = userService;
    }
    
    public ProcessDTO startProcess(String key, Map<String, Object> vars) {
        // 调用接口，Spring 注入 UserServiceImpl（单体时）
        UserDTO user = userService.getCurrentUser();
        // ...
    }
}
```

**方式 2：事件发布/监听（异步，松耦合）**

适用于不需要立即结果、解耦的场景。事件类定义在发布方的 api 模块中：

```java
// tenant-api：事件定义
public class TenantCreatedEvent implements Serializable {
    private Long tenantId;
    private String tenantName;
}

// tenant-impl：发布事件
eventPublisher.publish(new TenantCreatedEvent(tenantId, tenantName));

// organization-impl：监听事件，自动创建默认组织
@EventListener
public void onTenantCreated(TenantCreatedEvent event) {
    orgService.createDefaultDepartment(event.getTenantId());
}

// identity-impl：监听事件，创建默认管理员
@EventListener
public void onTenantCreated(TenantCreatedEvent event) {
    userService.createDefaultAdmin(event.getTenantId());
}
```

##### 模块设计约束

1. 模块间只通过 api 接口调用，不直接访问对方的 DAO/Entity
2. 每个模块有自己的表（表前缀区分），不跨模块直接查表
3. 共享内核最小化，只放真正共享的基础设施，不放业务接口
4. 模块依赖单向，禁止循环依赖
5. impl 模块绝不依赖其他 impl 模块
6. 每个模块是独立的限界上下文，领域边界清晰

#### 2.2.4 租户上下文机制

核心机制：通过 `ThreadLocal` 在请求生命周期内传递租户上下文，JPA `@TenantId` 自动隔离数据。

```
HTTP 请求进入
    │
    ▼
┌─────────────────────────────────────────┐
│  Portal 模块 - 请求拦截器                  │
│                                         │
│  1. 从 Token/请求头提取租户ID             │
│  2. 设置 TenantContext (ThreadLocal)     │
│  3. 请求转发到业务模块                     │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│  业务模块执行                              │
│                                         │
│  - JPA @TenantId 自动注入查询条件          │
│  - INSERT 自动填充 tenant_id              │
│  - 业务代码无感知                          │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│  请求结束 - 清理 TenantContext             │
└─────────────────────────────────────────┘
```

```java
// 共享内核：租户上下文
public class TenantContext {
    private static final ThreadLocal<TenantInfo> CONTEXT = 
        new ThreadLocal<>();
    
    public static void set(Long tenantId, String dataSourceKey) {
        CONTEXT.set(new TenantInfo(tenantId, dataSourceKey));
    }
    
    public static Long getTenantId() {
        return CONTEXT.get() != null ? 
            CONTEXT.get().getTenantId() : null;
    }
    
    public static String getDataSourceKey() {
        return CONTEXT.get() != null ? 
            CONTEXT.get().getDataSourceKey() : "shared";
    }
    
    public static void clear() {
        CONTEXT.remove();
    }
}

// 共享内核：数据源路由（预留多库扩展）
public class TenantRoutingDataSource 
        extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // 现阶段：所有租户走共享库
        return "shared";
        // 未来：大租户走独立库
        // return TenantContext.getDataSourceKey();
    }
}

// JPA 实体基类
@MappedSuperclass
public abstract class TenantEntity {
    @TenantId
    @Column(name = "tenant_id")
    private Long tenantId;
}

// 业务实体示例
@Entity
@Table(name = "biz_order")
public class Order extends TenantEntity {
    private String orderNo;
    private BigDecimal amount;
    // tenant_id 由父类和 @TenantId 自动处理
}
```

### 2.3 部署架构

#### 2.3.1 三种部署模式

```
模式一：共享部署（默认，多数租户）
┌─────────────────────────────────────┐
│  XCMS 应用实例（1个）                   │
│  ├─ 所有模块                          │
│  ├─ Flowable 引擎 × 1                │
│  └─ 数据库 × 1（tenant_id 隔离）      │
│                                     │
│  服务：租户A / B / C / D / E ...     │
└─────────────────────────────────────┘

模式二：大租户独立库（未来可选）
┌─────────────────────────────────────┐
│  XCMS 应用实例（1个）                   │
│  ├─ 所有模块                          │
│  ├─ Flowable 引擎 × 1                │
│  └─ 数据源路由：                      │
│     ├─ 共享库 → 小租户                │
│     └─ 独立库 → 大租户F              │
└─────────────────────────────────────┘

模式三：子公司独立部署（Tier 3）
┌──────────────────┐  ┌──────────────────┐
│  XCMS（共享）      │  │  子公司G XCMS（独立）│
│  ├─ 应用 + 引擎    │  │  ├─ 应用 + 引擎    │
│  └─ 共享数据库     │  │  └─ 独立数据库     │
│  服务：多数租户    │  │  服务：仅G         │
└────────┬─────────┘  └────────┬─────────┘
         │                     │
         │  元数据注册（不互通）  │
         └─────────────────────┘
```

#### 2.3.2 独立部署配置切换

同一份代码，通过配置文件切换部署模式：

```yaml
# 共享部署模式
platform:
  mode: shared
  multi-tenant: true
  datasource:
    type: shared
    url: jdbc:postgresql://db-host:5432/middle_platform

# 独立部署模式
platform:
  mode: dedicated
  multi-tenant: false          # 单租户模式，租户上下文固定
  tenant-id: 100               # 本实例绑定的租户ID
  datasource:
    type: dedicated
    url: jdbc:postgresql://g-db-host:5432/middle_platform_g
```

#### 2.3.3 独立部署与集团的关系

```
独立部署的子公司G              XCMS
┌──────────────────┐         ┌──────────────────┐
│  G的管理面        │         │  集团管理面        │
│  G的业务面        │         │                  │
│  G的独立数据库     │         │  共享数据库        │
└────────┬─────────┘         └────────┬─────────┘
         │                            │
         │  元数据对接（仅注册信息）     │
         │  ├─ G的租户基本信息          │
         │  ├─ G的组织架构（可选）       │
         │  └─ G的资源用量上报          │
         │                            │
         │  不互通                     │
         │  ├─ G的业务数据 ✗           │
         │  ├─ G的用户明细 ✗           │
         │  ├─ G的流程实例 ✗           │
         │  └─ G的消息数据 ✗           │
         │                            │
         │  预留扩展                   │
         │  └─ 未来需要时通过标准化API对接│
         └────────────────────────────┘
```

### 2.4 租户体系总览

#### 2.4.1 级联租户模型

```
集团（根租户，tenant_id=1）
├── 子公司A（一级租户，tenant_id=10）
│   ├── 产线A1（二级租户，tenant_id=101）
│   └── 产线A2（二级租户，tenant_id=102）
├── 子公司B（一级租户，tenant_id=20）
│   ├── 产线B1（二级租户，tenant_id=201）
│   └── 产线B2（二级租户，tenant_id=202）
├── 子公司C（一级租户，tenant_id=30，独立部署）
│   └── （C自管，与集团不互通业务数据）
├── 项目P1（项目型租户，tenant_id=500）
│   ├── 参与方：子公司A的某部门
│   └── 参与方：子公司B的某部门
└── 合作方X（外部合作方租户，tenant_id=600）
    └── （权限受限，仅参与指定流程）
```

**层级特性**：

| 特性 | 说明 |
|------|------|
| 层级深度 | 设计支持无限层级，现实不超过 10 级 |
| 父子关系 | 下级租户由上级创建，上级管理下级 |
| 管理可见 | 上级默认可管理下级（查看元数据/配置/用量） |
| 业务可见 | 上级默认不可见下级业务数据，需授权 |
| 配置继承 | 下级可继承上级配置，也可覆盖 |

#### 2.4.2 租户类型

| 类型 | 说明 | 管理面 | 业务面 | 创建下级 | 配额 |
|------|------|:---:|:---:|:---:|:---:|
| 组织型 | 集团/子公司/产线，常规组织实体 | ✅ 完整 | ✅ 完整 | ✅ 可创建 | 上级分配 |
| 项目型 | 跨组织的临时协作空间 | ⚠️ 项目内 | ✅ 完整 | ❌ | 上级分配 |
| 外部合作方 | 供应商/合作伙伴，权限受限 | ❌ | ⚠️ 受限 | ❌ | 上级分配 |

#### 2.4.3 租户配额管控

| 配额维度 | 说明 | 示例 |
|------|------|------|
| 用户数上限 | 租户可创建的最大用户数 | 子公司A：5000人 |
| 存储空间 | 文件存储总容量 | 子公司A：500GB |
| API调用量 | 单位时间API调用次数 | 子公司A：10000次/天 |
| 流程实例数 | 单位时间流程发起数 | 子公司A：5000个/月 |
| 功能开关 | 可使用的功能模块 | 子公司A：开启流程中心，关闭计量管理 |

配额由上级租户分配，可在配额范围内再分配给下级租户。

### 2.5 管理面与业务面分离

#### 2.5.1 分层管理面架构

```
                    ┌─────────────────┐
                    │  集团运营平台     │  ← 第0级管理面（上帝视角）
                    │  管全集团租户树   │
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐
              │                              │
     ┌────────┴────────┐           ┌────────┴────────┐
     │  子公司A管理后台   │           │  子公司B管理后台   │  ← 第1级管理面
     │  管A下属产线租户   │           │  管B下属产线租户   │
     └────────┬────────┘           └────────┬────────┘
              │                              │
       ┌──────┴──────┐                ┌──────┴──────┐
       │ 产线A1管理   │                │ 产线B1管理   │  ← 第2级管理面
       └─────────────┘                └─────────────┘
```

每一级管理面的职责：

| 职责 | 说明 |
|------|------|
| 租户管理 | 创建/停用/迁移下级租户 |
| 资源配额 | 为下级分配配额、功能开关 |
| 组织管理 | 管理本租户内组织架构、人员 |
| 权限配置 | 配置本租户内角色、数据权限 |
| 管理可见 | 查看下级租户结构、配置、用量（默认） |
| 业务可见授权 | 需要时授权查看下级业务数据（留审计） |
| 审计日志 | 记录所有管理操作 |

#### 2.5.2 管理可见 vs 业务可见

```
管理可见（默认拥有）：               业务可见（需授权）：
├─ 租户是否存在                      ├─ 订单明细
├─ 租户配置                          ├─ 客户信息
├─ 资源用量（CPU/存储/调用量）        ├─ 财务数据
├─ 用户数量                          ├─ 流程实例
├─ 组织架构                          └─ 业务报表
├─ 功能开关状态
└─ 审计日志
```

**业务可见授权流程**：

```
1. 管理员在管理面发起授权
   └─ 选择目标租户 + 数据范围 + 有效期 + 原因
2. 走审批流（高敏感必审）
3. 审批通过
   └─ 生成授权令牌（含范围+有效期）
   └─ 下发给业务面
4. 管理员查询业务数据
   └─ 请求带令牌 → 校验有效 → 放行
5. 到期自动回收，全程审计
```

#### 2.5.3 单体中的管理面与业务面

在单体多模块架构中，管理面与业务面是**逻辑分离**，通过 API 路径和权限控制实现：

```
┌─────────────────────────────────────────┐
│              单体应用                     │
│                                         │
│  管理API（/admin/**）                     │
│  ├─ /admin/tenant/*    租户管理           │
│  ├─ /admin/org/*       组织管理           │
│  ├─ /admin/user/*      用户管理           │
│  ├─ /admin/perm/*      权限管理           │
│  ├─ /admin/workflow/*  流程模板管理        │
│  ├─ /admin/config/*    配置管理           │
│  ├─ /admin/audit/*     审计管理           │
│  └─ 权限：管理角色可访问                   │
│      数据范围：管理可见（元数据/配置/用量） │
│                                         │
│  业务API（/api/**）                       │
│  ├─ /api/workflow/*    流程发起/审批       │
│  ├─ /api/message/*     消息收发           │
│  ├─ /api/file/*        文件管理           │
│  ├─ /api/portal/*      工作台             │
│  └─ 权限：业务角色可访问                   │
│      数据范围：本租户业务数据               │
│      跨租户：需业务可见令牌                 │
│                                         │
│  两套API在同一应用，靠角色+数据权限隔离     │
└─────────────────────────────────────────┘
```

---

