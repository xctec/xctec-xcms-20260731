# Non-Functional Requirements

## 8. 非功能性需求

### 8.1 性能与容量

#### 8.1.1 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 页面响应时间 | ≤ 2秒 | 95% 的请求 |
| API响应时间 | ≤ 500ms | 95% 的请求 |
| 流程发起 | ≤ 1秒 | 从提交到流程实例创建 |
| 文件上传（10MB） | ≤ 5秒 | 含存储写入 |
| 待办列表加载 | ≤ 1秒 | 含跨租户任务 |
| 并发用户 | 1000+ | 同时在线 |
| 日活用户 | 10000+ | 日活跃用户 |

#### 8.1.2 容量规划

| 维度 | 目标 | 扩展方式 |
|------|------|---------|
| 租户数量 | 1000+ | 单库 + tenant_id |
| 用户数量 | 100000+ | 单库可承载 |
| 流程实例 | 100万+/年 | 历史归档 |
| 文件存储 | 10TB+ | 对象存储扩展 |
| 审计日志 | 1000万+/年 | 定期归档 |

#### 8.1.3 性能优化策略

- **缓存**：热点数据（租户信息、权限配置、组织架构）缓存到 Redis
- **索引**：所有查询字段建立合适索引，特别是 tenant_id + 业务字段组合索引
- **分页**：所有列表查询强制分页
- **异步**：审计日志、消息发送、统计计算异步处理
- **历史归档**：已完成流程、过期审计日志定期归档到历史表

### 8.2 安全合规

#### 8.2.1 认证安全

| 要求 | 说明 |
|------|------|
| 密码加密 | BCrypt 加密存储 |
| 密码策略 | 最小8位，含大小写+数字+特殊字符 |
| 登录失败锁定 | 连续失败5次锁定30分钟 |
| 会话管理 | Token 有效期可配，支持强制下线 |
| HTTPS | 全站 HTTPS |
| CSRF防护 | 表单提交 CSRF Token |
| XSS防护 | 输入过滤、输出编码 |

#### 8.2.2 数据安全

| 要求 | 说明 |
|------|------|
| 租户隔离 | @TenantId 自动隔离，拦截器兜底 |
| 敏感字段加密 | 手机号、身份证等加密存储 |
| 字段脱敏 | 列级权限控制，按角色脱敏 |
| SQL注入防护 | JPA 参数化查询 |
| 数据导出管控 | 导出操作需权限 + 审计 |
| 跨租户审计 | 所有跨租户访问记录审计 |

#### 8.2.3 权限安全

- 最小权限原则：角色只授予必要的最小权限
| 权限定期审查 | 定期审查权限分配，清理冗余权限 |
| 权限变更审计 | 所有权限变更记录审计日志 |
| 敏感操作二次确认 | 删除、导出等操作二次确认 |
| 超时自动登出 | 闲置超时自动登出 |

### 8.3 高可用与容灾

#### 8.3.1 高可用部署

```
┌─────────────────────────────────────┐
│  负载均衡器                           │
│  ┌──────┬──────┐                    │
│  │实例1  │实例2  │  ← 应用多实例      │
│  └──┬───┴──┬───┘                    │
│     │      │                        │
│  ┌──┴──────┴──┐                     │
│  │  数据库主从  │  ← 主从复制         │
│  │  主  ←→  从 │                     │
│  └────────────┘                     │
│  ┌────────────┐                     │
│  │  Redis集群  │  ← 缓存高可用        │
│  └────────────┘                     │
└─────────────────────────────────────┘
```

| 组件 | 高可用方案 |
|------|----------|
| 应用实例 | 多实例 + 负载均衡，无状态 |
| 数据库 | 主从复制，故障自动切换 |
| Redis | 哨兵/集群模式 |
| 文件存储 | 对象存储多副本 |

#### 8.3.2 备份与恢复

| 数据 | 备份策略 | 保留 |
|------|---------|------|
| 数据库 | 每日全量 + 实时增量 | 30天 |
| 文件 | 对象存储多副本 | 永久 |
| 审计日志 | 每日归档 | 按策略（≥1年） |
| 配置 | 版本化存储 | 永久 |

### 8.4 可扩展性

#### 8.4.1 架构演进路径

```
阶段1（当前）：单体多模块
│  所有模块在一个JAR，模块间方法调用
│  数据：单库 + tenant_id 隔离
│  部署：多实例 + 负载均衡
│
│  触发拆分条件：
│  ├ 某模块负载远高于其他
│  ├ 某模块需要独立技术栈
│  └ 某模块需要独立发版
│
▼
阶段2（按需）：单体 + 少数独立服务
│  例如：流程中心拆出（CPU密集）
│  其余仍在单体
│  模块间调用 → HTTP/RPC
│
▼
阶段3（成熟）：完整微服务
   按需拆分更多模块
   引入网关、注册中心、链路追踪
```

#### 8.4.2 API 模块演进：从单体到微服务

API 模块模式的核心价值：**从单体到微服务，消费者代码不用改**。接口定义（api 模块）不变，只是注入的实现从本地变为远程。

##### 单体阶段

```
┌─────────────────────────────────────────────┐
│              一个 JVM（app模块启动）            │
│                                             │
│  workflow-impl                              │
│    @Autowired UserService userService;      │
│         │                                   │
│         │ Spring注入                        │
│         ▼                                   │
│  identity-impl                              │
│    UserServiceImpl（真实实现，直接方法调用）    │
│         │                                   │
│         ▼                                   │
│  UserRepository → 数据库                     │
│                                             │
│  依赖：workflow-impl → identity-api          │
│  注入：identity-impl 提供实现                 │
└─────────────────────────────────────────────┘
```

##### 微服务阶段（workflow 拆出去）

```
┌──────────────────────┐    ┌──────────────────────┐
│  identity 服务        │    │  workflow 服务        │
│  (identity-server)   │    │  (workflow-server)   │
│                      │    │                      │
│  identity-impl       │    │  workflow-impl       │
│    UserServiceImpl   │    │    @Autowired        │
│         │            │    │    UserService       │
│         ▼            │    │         │            │
│  UserRepository→DB   │    │         │ 注入       │
│                      │    │         ▼            │
│  UserController      │    │  identity-client     │
│  (REST端点)          │    │    UserServiceClient │
│    GET /api/users/1  │    │    (发HTTP请求)      │
│         │            │    │         │            │
│         ▼            │    │         ▼            │
│    返回 UserDTO      │◄───│   HTTP GET /api/users/1│
│                      │    │   (带租户头)         │
└──────────────────────┘    └──────────────────────┘
```

##### 模块变化对比

```
                        单体时              微服务时
                        ──────              ────────
identity-api            有                  有（不变，发布到Maven仓库共享）
identity-impl           有                  有（不变，在identity服务中）
identity-server         无（app统一启动）     有（新增，独立启动+REST端点）
identity-client         无（不需要）          有（新增，远程调用实现）  ← 关键新增

workflow-impl           有                  有（代码不变！）
workflow依赖            identity-impl        identity-client
```

##### 代码示例

**identity-api（两个阶段完全不变）**：

```java
// identity-api 模块：接口定义（供应方和调用方共享）
public interface UserService {
    UserDTO getUserById(Long userId);
    UserDTO getCurrentUser();
    List<UserDTO> listUsers(UserQuery query);
}
```

**供应方：identity-impl（真实实现，两个阶段不变）**：

```java
// identity-impl 模块：真实实现
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    
    public UserDTO getUserById(Long userId) {
        UserEntity entity = repo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        return UserMapper.toDTO(entity);
    }
}

// identity-server 模块：REST 控制器（微服务时新增）
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    
    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
```

**调用方：identity-client（微服务时新增，替代直接注入impl）**：

```java
// identity-client 模块：远程实现（实现同一个接口，但发HTTP请求）
public class UserServiceClient implements UserService {
    private final RestClient httpClient;
    
    public UserDTO getUserById(Long userId) {
        return httpClient.get()
            .uri("/api/users/" + userId)
            .header("X-Tenant-Id", TenantContext.getTenantId().toString())
            .retrieve()
            .body(UserDTO.class);
    }
}
```

**调用方：workflow-impl（两个阶段代码完全不变！）**：

```java
// workflow-impl 模块：消费者代码
@Service
public class ProcessServiceImpl implements ProcessService {
    private final UserService userService;  // 依赖接口，不关心实现
    
    public ProcessServiceImpl(UserService userService) {
        this.userService = userService;
    }
    
    public ProcessDTO startProcess(String key, Map<String, Object> vars) {
        // 这行代码在单体和微服务中完全一样
        UserDTO user = userService.getCurrentUser();
        // 单体时：调用 UserServiceImpl → 直接方法调用
        // 微服务时：调用 UserServiceClient → HTTP请求
        // 但 ProcessServiceImpl 不知道也不关心
    }
}
```

##### OpenFeign 方案（更简洁）

使用 Spring Cloud OpenFeign，连 identity-client 的手写实现都不需要：

```java
// identity-api：接口 + Feign注解（同时服务供应方和调用方）
@FeignClient(name = "identity-service", path = "/api/users")
public interface UserService {
    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") Long userId);
    
    @GetMapping("/current")
    UserDTO getCurrentUser();
}

// 供应方：控制器直接实现接口
@RestController
@RequestMapping("/api/users")
public class UserController implements UserService {
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}

// 调用方：直接注入，Feign自动生成远程实现
@Service
public class ProcessServiceImpl implements ProcessService {
    @Autowired
    private UserService userService;  // Feign自动生成HTTP客户端
    
    // 代码完全不变
}
```

- 单体时：不加 `@EnableFeignClients`，Spring 注入本地 `UserServiceImpl`
- 微服务时：加 `@EnableFeignClients`，Feign 注入自动生成的远程客户端

##### 事件通信演进

事件类定义在 api 模块中不变，传输方式随架构演进：

```
单体阶段（进程内事件）：
┌─────────────────────────────────────┐
│              一个 JVM                │
│                                     │
│  identity-impl                      │
│    eventPublisher.publish(           │
│      new UserCreatedEvent(...))     │
│         │                           │
│         │ Spring ApplicationEvent   │
│         ▼                           │
│  workflow-impl                      │
│    @EventListener                   │
│    onUserCreated(event)             │
└─────────────────────────────────────┘

微服务阶段（消息队列）：
┌──────────────────┐    ┌──────────────────┐
│  identity 服务    │    │  workflow 服务    │
│                  │    │                  │
│  kafkaTemplate   │    │  @KafkaListener  │
│    .send(         │    │    onUserCreated │
│      "user-events"│    │    (event)       │
│      , event)     │    │                  │
│        │         │    │        ▲         │
│        ▼         │    │        │         │
│  ┌──────────┐    │    │  ┌──────────┐    │
│  │  Kafka   │────┼────┼─→│  Kafka   │    │
│  │  Topic   │    │    │  │ Consumer │    │
│  └──────────┘    │    │  └──────────┘    │
└──────────────────┘    └──────────────────┘
```

```java
// identity-api：事件定义（两个阶段不变）
public class UserCreatedEvent implements Serializable {
    private Long userId;
    private Long tenantId;
    private String username;
}

// 单体 - 发布方：Spring事件
applicationEventPublisher.publishEvent(new UserCreatedEvent(...));

// 微服务 - 发布方：Kafka
kafkaTemplate.send("user-events", new UserCreatedEvent(...));

// 单体 - 消费方：Spring监听
@EventListener
public void handle(UserCreatedEvent event) { ... }

// 微服务 - 消费方：Kafka监听
@KafkaListener(topics = "user-events")
public void handle(UserCreatedEvent event) { ... }
```

##### 演进总结

```
┌──────────────────────────────────────────────────────┐
│                  模块通信演进原则                       │
│                                                      │
│  identity-api（接口+DTO+事件）                        │
│  ├── 单体时：供应方和调用方共享，同一JVM内注入          │
│  ├── 微服务时：发布到Maven仓库，两个服务都依赖          │
│  └── 从不变更                                        │
│                                                      │
│  供应方（identity服务）：                              │
│  ├── 单体时：identity-impl（真实实现）                 │
│  ├── 微服务时：identity-impl + identity-server(REST)  │
│  └── 实现代码不变，新增REST端点                        │
│                                                      │
│  调用方（workflow服务）：                              │
│  ├── 单体时：注入 identity-impl 的真实实现             │
│  ├── 微服务时：注入 identity-client 的远程实现         │
│  └── 消费者代码不变，只换注入的Bean                    │
│                                                      │
│  事件通信：                                           │
│  ├── 单体时：Spring ApplicationEvent（进程内）         │
│  ├── 微服务时：Kafka/RabbitMQ（跨进程）               │
│  └── 事件类不变，只换传输方式                          │
│                                                      │
│  核心结论：                                           │
│  API模块是契约，契约不变，实现可替换                    │
│  这就是从单体到微服务平滑演进的关键                     │
└──────────────────────────────────────────────────────┘
```

#### 8.4.3 数据库扩展路径

```
阶段1（当前）：单库 + tenant_id
│  一个数据库实例，所有租户共享
│  AbstractRoutingDataSource 预留
│
│  触发条件：某租户需要独立库
│
▼
阶段2：单库 + 个别租户独立库
│  共享库：大多数租户
│  独立库：大租户（路由切换）
│
│  触发条件：子公司独立部署
│
▼
阶段3：共享库 + 独立库 + 独立部署
   共享库：小租户
   独立库：大租户
   独立部署：子公司自带完整应用+库
```

#### 8.4.4 模块可拆分性保障

| 约束 | 说明 |
|------|------|
| 模块间接口调用 | 通过 api 模块接口调用，不直接访问对方 DAO/Entity |
| impl 不互相依赖 | impl 只依赖 api，绝不依赖其他 impl |
| 表归属 | 每个模块有自己的表，不跨模块查表 |
| 共享内核最小化 | 只放全局共享的基础设施，不放业务接口 |
| 依赖单向 | 禁止循环依赖 |
| 领域边界清晰 | 每个模块是独立的限界上下文 |

---

