# API Design: Tenant Management

> 租户管理 API 接口定义 + shared-kernel 基础类

## 1. shared-kernel 基础类

### 1.1 TenantContext

```java
package com.xcms.kernel.context;

/**
 * 租户上下文，通过 ThreadLocal 在请求生命周期内传递租户信息
 */
public class TenantContext {
    private static final ThreadLocal<TenantInfo> CONTEXT = new ThreadLocal<>();

    public static void set(Long tenantId, String dataSourceKey) {
        CONTEXT.set(new TenantInfo(tenantId, dataSourceKey));
    }

    public static Long getTenantId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.tenantId() : null;
    }

    public static String getDataSourceKey() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.dataSourceKey() : "shared";
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /** 临时切换租户上下文（跨租户操作） */
    public static TenantInfo switchTo(Long tenantId) {
        TenantInfo original = CONTEXT.get();
        set(tenantId, "shared");
        return original;
    }

    public static void restore(TenantInfo info) {
        if (info != null) {
            CONTEXT.set(info);
        } else {
            clear();
        }
    }

    public record TenantInfo(Long tenantId, String dataSourceKey) {}
}
```

### 1.2 TenantEntity

```java
package com.xcms.kernel.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;

@MappedSuperclass
public abstract class TenantEntity {

    @TenantId
    @Column(name = "tenant_id")
    private Long tenantId;

    // getter/setter
}
```

### 1.3 BaseEntity

```java
package com.xcms.kernel.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### 1.4 通用响应类

```java
package com.xcms.kernel.common;

// 统一API响应
public class ApiResponse<T> {
    private int code;       // 0=成功, 非0=错误码
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> error(int code, String message) { ... }
}

// 分页结果
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) { ... }
}

// 分页查询基类
public class PageQuery {
    private int page = 1;
    private int size = 20;
    private String sortBy;
    private String sortOrder = "ASC";
}
```

### 1.5 异常类

```java
package com.xcms.kernel.exception;

public class BusinessException extends RuntimeException {
    private int code;
    public BusinessException(int code, String message) { ... }
}

public class PermissionException extends BusinessException {
    public PermissionException(String message) { super(403, message); }
}

public class NotFoundException extends BusinessException {
    public NotFoundException(String resource, Long id) { super(404, resource + " not found: " + id); }
}
```

### 1.6 数据源路由

```java
package com.xcms.kernel.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // 现阶段：所有租户走共享库
        return "shared";
        // 未来：大租户走独立库
        // return TenantContext.getDataSourceKey();
    }
}
```

---

## 2. tenant-api 接口定义

### 2.1 TenantService

```java
package com.xcms.tenant.api;

public interface TenantService {

    /**
     * 创建下级租户
     * @param request 创建请求
     * @return 租户信息
     */
    TenantDTO createTenant(TenantCreateRequest request);

    /**
     * 更新租户信息
     */
    TenantDTO updateTenant(Long tenantId, TenantUpdateRequest request);

    /**
     * 获取租户详情
     */
    TenantDTO getTenantById(Long tenantId);

    /**
     * 根据编码获取租户
     */
    TenantDTO getTenantByCode(String tenantCode);

    /**
     * 获取租户树（当前租户的子树）
     */
    List<TenantTreeDTO> getTenantTree(Long rootTenantId);

    /**
     * 获取子租户列表
     */
    PageResult<TenantDTO> listSubTenants(Long parentId, TenantQuery query);

    /**
     * 启用/停用/锁定租户
     */
    void changeTenantStatus(Long tenantId, TenantStatus status);

    /**
     * 迁移租户到新的父租户
     */
    void migrateTenant(Long tenantId, Long newParentId);

    /**
     * 获取租户路径上的所有祖先租户（含自身）
     */
    List<TenantDTO> getTenantAncestors(Long tenantId);

    /**
     * 检查是否为祖先租户（用于权限判断）
     */
    boolean isAncestor(Long ancestorTenantId, Long descendantTenantId);
}
```

### 2.2 TenantQuotaService

```java
package com.xcms.tenant.api;

public interface TenantQuotaService {

    /**
     * 分配配额给下级租户
     */
    void allocateQuota(Long tenantId, QuotaAllocateRequest request);

    /**
     * 获取租户配额列表
     */
    List<TenantQuotaDTO> getQuotas(Long tenantId);

    /**
     * 检查配额是否可用
     */
    boolean checkQuota(Long tenantId, QuotaType quotaType, long amount);

    /**
     * 消耗配额（用户创建、文件上传等时调用）
     */
    void consumeQuota(Long tenantId, QuotaType quotaType, long amount);

    /**
     * 释放配额（用户删除、文件删除等时调用）
     */
    void releaseQuota(Long tenantId, QuotaType quotaType, long amount);

    /**
     * 获取配额使用情况
     */
    QuotaUsageDTO getQuotaUsage(Long tenantId);
}
```

### 2.3 TenantFeatureService

```java
package com.xcms.tenant.api;

public interface TenantFeatureService {

    /**
     * 获取租户功能开关列表
     */
    List<TenantFeatureDTO> getFeatures(Long tenantId);

    /**
     * 启用/禁用功能
     */
    void toggleFeature(Long tenantId, String featureCode, boolean enabled);

    /**
     * 检查功能是否启用
     */
    boolean isFeatureEnabled(Long tenantId, String featureCode);

    /**
     * 获取功能配置
     */
    String getFeatureConfig(Long tenantId, String featureCode);
}
```

### 2.4 DTO 定义

```java
package com.xcms.tenant.api.dto;

// 租户信息
public class TenantDTO {
    private Long id;
    private String tenantCode;
    private String tenantName;
    private TenantType tenantType;     // ORGANIZATION/PROJECT/EXTERNAL
    private Long parentId;
    private Integer level;
    private String path;
    private TenantStatus status;
    private String deploymentMode;     // SHARED/DEDICATED
    private LocalDateTime createdAt;
}

// 创建租户请求
public class TenantCreateRequest {
    private String tenantCode;         // 必填，唯一
    private String tenantName;         // 必填
    private TenantType tenantType;     // 必填
    private Long parentId;             // 必填（根租户除外）
    private List<QuotaAllocateRequest> quotas;  // 配额分配
    private Map<String, Boolean> features;      // 功能开关
}

// 更新租户请求
public class TenantUpdateRequest {
    private String tenantName;
    private TenantStatus status;
}

// 租户树
public class TenantTreeDTO {
    private Long id;
    private String tenantName;
    private TenantType tenantType;
    private TenantStatus status;
    private List<TenantTreeDTO> children;
}

// 租户查询
public class TenantQuery extends PageQuery {
    private String keyword;
    private TenantType tenantType;
    private TenantStatus status;
}

// 配额分配请求
public class QuotaAllocateRequest {
    private QuotaType quotaType;       // USER_COUNT/STORAGE/API_CALL/PROCESS_INSTANCE
    private Long quotaLimit;
    private String period;             // DAILY/MONTHLY/TOTAL
}

// 配额信息
public class TenantQuotaDTO {
    private Long id;
    private Long tenantId;
    private QuotaType quotaType;
    private Long quotaLimit;
    private Long quotaUsed;
    private Long allocatedTo;
    private String period;
}

// 配额使用情况
public class QuotaUsageDTO {
    private Long tenantId;
    private List<QuotaUsageItem> items;
}

public class QuotaUsageItem {
    private QuotaType quotaType;
    private Long limit;
    private Long used;
    private Double usagePercentage;
}

// 功能开关
public class TenantFeatureDTO {
    private Long id;
    private Long tenantId;
    private String featureCode;
    private Boolean enabled;
    private String config;
}

// 枚举
public enum TenantType {
    ORGANIZATION, PROJECT, EXTERNAL
}

public enum TenantStatus {
    ACTIVE, SUSPENDED, LOCKED, MIGRATING, ARCHIVED
}

public enum QuotaType {
    USER_COUNT, STORAGE, API_CALL, PROCESS_INSTANCE
}
```

### 2.5 事件定义

```java
package com.xcms.tenant.api.event;

// 租户创建事件（organization/identity 模块监听，初始化默认组织和管理员）
public class TenantCreatedEvent implements Serializable {
    private Long tenantId;
    private String tenantCode;
    private String tenantName;
    private TenantType tenantType;
    private Long parentId;
}

// 租户停用事件
public class TenantSuspendedEvent implements Serializable {
    private Long tenantId;
    private String reason;
}

// 租户迁移事件
public class TenantMigratedEvent implements Serializable {
    private Long tenantId;
    private Long oldParentId;
    private Long newParentId;
}
```

---

## 3. DDL

参见 [db/ddl/01-tenant-management.sql](../../db/ddl/01-tenant-management.sql)
