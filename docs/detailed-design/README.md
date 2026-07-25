# XCMS API Design

> 详细设计 - API 接口定义

## Index

| Document | Module | Description |
|----------|--------|-------------|
| [01-api-tenant-management.md](01-api-tenant-management.md) | tenant-management | 租户管理 API + shared-kernel 基础类 |
| [02-api-organization-identity.md](02-api-organization-identity.md) | organization + identity | 组织架构 + 身份认证 API |
| [03-api-authorization.md](03-api-authorization.md) | authorization | 权限中心 API |
| [04-api-workflow.md](04-api-workflow.md) | workflow | 流程中心 API |
| [05-api-shared-services.md](05-api-shared-services.md) | message + config + file + task + audit | 共享服务 API |

## Conventions

- API 接口定义在 `xxx-api` 模块中，实现在 `xxx-impl` 模块中
- 所有需要租户隔离的实体继承 `TenantEntity`
- DTO 命名：`XxxDTO`（查询返回）、`XxxCreateRequest`（创建）、`XxxUpdateRequest`（更新）
- 查询参数命名：`XxxQuery`
- 事件命名：`XxxCreatedEvent`、`XxxUpdatedEvent`、`XxxDeletedEvent`
- 分页返回统一用 `PageResult<T>`
- API 返回统一用 `ApiResponse<T>`

## API Style Specification

### 核心原则：全部使用 POST

- **所有接口必须显式使用 `@PostMapping`**，不使用 `@GetMapping`、`@PutMapping`、`@DeleteMapping`
- **禁用 PUT 和 DELETE 方法**，代码层面不提供这些端点
- **GET 仅限必要场景**：文件下载、图片预览等流式响应场景可用 `@GetMapping`，但需在文档中说明理由
- **所有参数通过 `@RequestBody` 传递**，不使用 query string、不使用 `@PathVariable`（ID 放 body 中）
- **暂不加版本号前缀**，后续需要时再加 `/v1/` 等

### URL 结构

```
/{admin|api}/{module}/{action}

管理面：/admin/{module}/{action}
业务面：/api/{module}/{action}
```

### URL 命名规范

| 规则 | 说明 | 示例 |
|------|------|------|
| HTTP 方法 | 全部 POST（GET 仅限流式响应场景） | `@PostMapping("/list")` |
| 模块名 | 单数，小写 | `tenant`、`user`、`workflow` |
| 动作名 | kebab-case（小写连字符） | `change-status`、`my-list`、`read-all` |
| 参数位置 | 全部在 `@RequestBody`（JSON） | `{ "id": 101, "status": "ACTIVE" }` |
| 分页参数 | 在 body 中 | `{ "page": 1, "size": 20 }` |
| 管理面前缀 | `/admin/**` | `/admin/tenant/list` |
| 业务面前缀 | `/api/**` | `/api/workflow/task/my-list` |
| 返回格式 | 统一 `ApiResponse<T>` | `{ "code": 0, "message": "success", "data": {...} }` |

### URL 示例

```
# 管理面
POST /admin/tenant/list              body: { "page": 1, "size": 20, "keyword": "A" }
POST /admin/tenant/create            body: { "tenantCode": "A001", "tenantName": "子公司A" }
POST /admin/tenant/get               body: { "id": 101 }
POST /admin/tenant/update            body: { "id": 101, "tenantName": "新名称" }
POST /admin/tenant/delete            body: { "id": 101 }
POST /admin/tenant/change-status     body: { "id": 101, "status": "SUSPENDED" }
POST /admin/tenant/migrate           body: { "id": 101, "newParentId": 20 }
POST /admin/tenant/tree              body: { "rootId": 1 }

# 业务面
POST /api/workflow/process/start     body: { "processKey": "purchase", "variables": {...} }
POST /api/workflow/task/my-list      body: { "page": 1, "size": 20 }
POST /api/workflow/task/complete     body: { "taskId": "xxx", "comment": "同意" }
POST /api/message/list               body: { "page": 1, "size": 20, "msgType": "FLOW" }
POST /api/message/read               body: { "ids": [1, 2, 3] }
POST /api/file/upload                body: multipart（文件 + folderId）
POST /api/file/delete                body: { "id": 101 }

# GET 仅限流式响应场景
GET  /api/file/download/{token}      文件下载（流式响应，允许 GET）
```

### Controller 编写规范

```java
@RestController
@RequestMapping("/admin/tenant")
public class TenantAdminController {

    // 所有方法必须用 @PostMapping
    @PostMapping("/list")
    public ApiResponse<PageResult<TenantDTO>> list(@RequestBody TenantQuery query) {
        return ApiResponse.success(tenantService.listTenants(query));
    }

    @PostMapping("/create")
    public ApiResponse<TenantDTO> create(@RequestBody TenantCreateRequest request) {
        return ApiResponse.success(tenantService.createTenant(request));
    }

    @PostMapping("/get")
    public ApiResponse<TenantDTO> get(@RequestBody IdRequest request) {
        return ApiResponse.success(tenantService.getTenantById(request.getId()));
    }

    @PostMapping("/update")
    public ApiResponse<TenantDTO> update(@RequestBody TenantUpdateRequest request) {
        return ApiResponse.success(tenantService.updateTenant(request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdRequest request) {
        tenantService.deleteTenant(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/change-status")
    public ApiResponse<Void> changeStatus(@RequestBody ChangeStatusRequest request) {
        tenantService.changeTenantStatus(request.getId(), request.getStatus());
        return ApiResponse.success();
    }

    // GET 仅限流式响应场景（如文件下载），需注释说明理由
    // @GetMapping("/export")  // 导出文件，流式响应，允许 GET
    // public void export(@RequestParam Long id, HttpServletResponse response) { ... }
}
```

### 通用请求体

```java
// ID 请求（get/delete 等只需要 id 的场景）
public class IdRequest {
    private Long id;
}

// 批量 ID 请求
public class IdsRequest {
    private List<Long> ids;
}

// 分页查询基类（已有 PageQuery）
public class PageQuery {
    private int page = 1;
    private int size = 20;
    private String sortBy;
    private String sortOrder = "ASC";
}
```

## Entity ↔ DTO Mapping (MapStruct)

### 规范

- **Entity 永远不暴露到 API 层**，所有转换通过 MapStruct Mapper
- Mapper 接口放在 `xxx-impl/mapper/` 目录
- Mapper 使用 `@Mapper(componentModel = "spring")` 声明为 Spring Bean
- 敏感字段（password 等）用 `@Mapping(target = "xxx", ignore = true)` 忽略
- 字段名不同时用 `@Mapping(target = "xxx", source = "yyy")` 显式映射
- 与 Lombok 配合时需配置 annotation processor 顺序

### 模块组织

```
xxx-impl/
├── entity/
│   └── XxxEntity.java         ← JPA 实体（不对外暴露）
├── mapper/
│   └── XxxMapper.java         ← MapStruct 转换接口
├── repository/
│   └── XxxRepository.java     ← JPA Repository
└── service/
    └── XxxServiceImpl.java    ← Service 实现，调用 Mapper
```

### 基本用法

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity → DTO
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    UserDTO toDTO(UserEntity entity);

    List<UserDTO> toDTOList(List<UserEntity> entities);

    // CreateRequest → Entity（创建时）
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)        // @TenantId 自动填充
    @Mapping(target = "password", ignore = true)         // 单独加密
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)        // @PrePersist 填充
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(UserCreateRequest request);

    // UpdateRequest → Entity（更新时，部分更新）
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget UserEntity entity);

    @Named("statusToString")
    default String statusToString(UserStatus status) {
        return status != null ? status.name() : null;
    }
}
```

### Service 使用

```java
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserDTO createUser(UserCreateRequest request) {
        UserEntity entity = mapper.toEntity(request);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity = repository.save(entity);
        return mapper.toDTO(entity);
    }

    public UserDTO updateUser(Long userId, UserUpdateRequest request) {
        UserEntity entity = repository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        mapper.updateEntity(request, entity);    // 部分更新
        entity = repository.save(entity);
        return mapper.toDTO(entity);
    }

    public PageResult<UserDTO> listUsers(UserQuery query) {
        Page<UserEntity> page = repository.findAll(buildSpec(query),
            PageRequest.of(query.getPage() - 1, query.getSize()));
        return PageResult.of(
            mapper.toDTOList(page.getContent()),   // 批量转换
            page.getTotalElements(),
            query.getPage(), query.getSize()
        );
    }
}
```

### 与字段级权限配合

MapStruct 负责转换，脱敏在转换之后：

```java
// 方式1：手动脱敏（Service 中调用）
public CustomerDTO getCustomer(Long id, Long userId) {
    CustomerEntity entity = repository.findById(id).orElseThrow(...);
    CustomerDTO dto = mapper.toDTO(entity);       // MapStruct 转换
    maskService.apply(dto, userId, "Customer");    // 字段级脱敏
    return dto;
}

// 方式2：自动脱敏（ResponseBodyAdvice，推荐）
// Service 只管转换，脱敏交给 @MaskResource 注解
@MaskResource("Customer")
public ApiResponse<CustomerDTO> getCustomer(@PathVariable Long id) {
    CustomerDTO dto = customerService.getCustomer(id);
    return ApiResponse.success(dto);  // 返回前自动脱敏
}
```

### 与 Lombok 配合

Maven 需配置 annotation processor 顺序：

```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.6.0</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.30</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```
