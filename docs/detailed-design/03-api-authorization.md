# API Design: Authorization

> 权限中心 API 接口定义

## 1. auth-api 接口定义

### 1.1 PermissionService（权限校验，其他模块调用）

```java
package com.xcms.auth.api;

public interface PermissionService {

    /**
     * 检查用户是否有指定操作权限
     * @param userId 用户ID
     * @param permCode 权限编码（如 "order:create"）
     * @return true=有权限
     */
    boolean checkPermission(Long userId, String permCode);

    /**
     * 检查用户是否有指定操作权限，无权限则抛异常
     */
    void requirePermission(Long userId, String permCode);

    /**
     * 获取用户的所有操作权限编码
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户可见的菜单树
     */
    List<MenuDTO> getUserMenus(Long userId, MenuScope scope);

    /**
     * 检查用户对指定租户的业务可见权限
     * @param userId 用户ID
     * @param targetTenantId 目标租户ID
     * @return 业务可见授权信息，null=无授权
     */
    BusinessVisibilityAuth checkBusinessVisibility(Long userId, Long targetTenantId);
}
```

### 1.2 DataPermissionService（数据权限，其他模块调用）

```java
package com.xcms.auth.api;

import org.springframework.data.jpa.domain.Specification;

public interface DataPermissionService {

    /**
     * 获取用户的数据权限 Specification（行级权限）
     * 用于 JPA 查询时自动过滤数据范围
     *
     * @param userId 用户ID
     * @param resourceType 资源类型（如 Order.class.getSimpleName()）
     * @return 数据权限 Specification，组合到查询中
     */
    <T> Specification<T> getDataScopeSpec(Long userId, String resourceType);

    /**
     * 获取用户的数据权限上下文（用于非 JPA 场景）
     */
    DataPermissionContext getDataPermissionContext(Long userId, String resourceType);

    /**
     * 对查询结果应用列级权限（字段脱敏/隐藏）
     */
    <T> T applyColumnMask(T entity, Long userId, String resourceType);

    /**
     * 对列表结果应用列级权限
     */
    <T> List<T> applyColumnMask(List<T> entities, Long userId, String resourceType);
}
```

### 1.3 DataPermissionContext（数据权限上下文）

```java
package com.xcms.auth.api;

/**
 * 数据权限上下文，包含用户在各维度的数据范围
 */
public class DataPermissionContext {
    private Long userId;
    private Long tenantId;
    private String resourceType;

    // 行级权限各维度
    private List<String> orgPaths;           // 组织路径（如 /1/10/）
    private List<Long> businessLineIds;      // 业务线
    private List<String> regions;            // 地域
    private List<String> tags;               // 自定义标签
    private LocalDateTime timeFrom;          // 时间范围起
    private LocalDateTime timeTo;            // 时间范围止
    private boolean ownerOnly;               // 仅看自己创建/负责的

    public boolean hasOrgScope() { return orgPaths != null && !orgPaths.isEmpty(); }
    public boolean hasBusinessLineScope() { return businessLineIds != null && !businessLineIds.isEmpty(); }
    public boolean hasRegionScope() { return regions != null && !regions.isEmpty(); }
    public boolean hasTagScope() { return tags != null && !tags.isEmpty(); }
    public boolean hasTimeScope() { return timeFrom != null || timeTo != null; }
    public boolean hasOwnerScope() { return ownerOnly; }
}
```

### 1.4 RolePermissionService（角色权限管理，管理面调用）

```java
package com.xcms.auth.api;

public interface RolePermissionService {

    /**
     * 为角色分配权限（菜单/操作）
     */
    void assignPermissionsToRole(Long roleId, List<PermissionAssignRequest> permissions);

    /**
     * 移除角色的权限
     */
    void removePermissionsFromRole(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的权限列表
     */
    List<PermissionDTO> getRolePermissions(Long roleId);
}
```

### 1.5 DataRuleService（数据规则管理，管理面调用）

```java
package com.xcms.auth.api;

public interface DataRuleService {

    /**
     * 创建数据权限规则
     */
    DataRuleDTO createRule(DataRuleCreateRequest request);

    /**
     * 更新数据权限规则
     */
    DataRuleDTO updateRule(Long ruleId, DataRuleUpdateRequest request);

    /**
     * 删除数据权限规则
     */
    void deleteRule(Long ruleId);

    /**
     * 获取规则列表
     */
    List<DataRuleDTO> listRules(String resourceType);

    /**
     * 将规则绑定到角色
     */
    void bindRuleToRole(Long ruleId, Long roleId, String scopeValue);

    /**
     * 解绑规则与角色
     */
    void unbindRuleFromRole(Long ruleId, Long roleId);

    /**
     * 模拟用户验证数据权限
     */
    DataPermissionContext testPermission(Long userId, String resourceType);
}
```

### 1.6 ColumnMaskService（列级脱敏管理）

```java
package com.xcms.auth.api;

public interface ColumnMaskService {

    ColumnMaskDTO createMaskRule(ColumnMaskCreateRequest request);
    ColumnMaskDTO updateMaskRule(Long ruleId, ColumnMaskUpdateRequest request);
    void deleteMaskRule(Long ruleId);
    List<ColumnMaskDTO> listMaskRules(String resourceType);
}
```

### 1.7 BusinessVisibilityAuthService（业务可见授权）

```java
package com.xcms.auth.api;

public interface BusinessVisibilityAuthService {

    /**
     * 发起业务可见授权
     */
    CrossTenantAuthDTO requestAuthorization(CrossTenantAuthRequest request);

    /**
     * 审批授权
     */
    void approveAuthorization(Long authId, Long approverId);

    /**
     * 拒绝授权
     */
    void rejectAuthorization(Long authId, Long approverId, String reason);

    /**
     * 撤销授权
     */
    void revokeAuthorization(Long authId);

    /**
     * 获取授权列表
     */
    PageResult<CrossTenantAuthDTO> listAuthorizations(CrossTenantAuthQuery query);

    /**
     * 获取有效授权令牌
     */
    CrossTenantAuthDTO getActiveAuth(Long userId, Long targetTenantId);

    /**
     * 清理过期授权
     */
    void cleanupExpiredAuthorizations();
}
```

### 1.8 DTO 定义

```java
package com.xcms.auth.api.dto;

public class MenuDTO {
    private Long id;
    private String menuCode;
    private String menuName;
    private String menuType;     // CATALOG/MENU/BUTTON
    private String path;
    private String icon;
    private Integer sortOrder;
    private List<MenuDTO> children;
}

public class PermissionDTO {
    private Long id;
    private String permCode;
    private String permName;
    private String permType;     // MENU/OPERATION
    private String module;
    private String action;
}

public class PermissionAssignRequest {
    private Long permId;
    private String permType;     // MENU/OPERATION
    private String scopeConfig;  // JSON, 数据权限配置
}

public class DataRuleDTO {
    private Long id;
    private String ruleName;
    private String ruleType;     // ROW/COLUMN/CUSTOM
    private String resourceType;
    private String dimension;    // ORG/BUSINESS_LINE/REGION/TAG/TIME/OWNER
    private String ruleConfig;   // JSON
    private Integer priority;
    private String status;
}

public class DataRuleCreateRequest {
    private String ruleName;
    private String ruleType;
    private String resourceType;
    private String dimension;
    private String ruleConfig;   // JSON
    private Integer priority;
}

public class ColumnMaskDTO {
    private Long id;
    private String resourceType;
    private String fieldName;
    private String maskType;     // HIDE/MASK/PARTIAL
    private String maskRule;
    private List<Long> roleIds;
}

public class ColumnMaskCreateRequest {
    private String resourceType;
    private String fieldName;
    private String maskType;
    private String maskRule;
    private List<Long> roleIds;
}

public class CrossTenantAuthDTO {
    private Long id;
    private Long tenantId;           // 发起租户
    private Long targetTenantId;     // 目标租户
    private Long userId;
    private String userName;
    private String dataScope;        // JSON
    private String token;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String status;           // PENDING/APPROVED/ACTIVE/EXPIRED/REVOKED
    private Long approvedBy;
    private String reason;
}

public class CrossTenantAuthRequest {
    private Long targetTenantId;
    private Long userId;
    private String dataScope;        // JSON, 数据范围
    private LocalDateTime validUntil;
    private String reason;
}

public class CrossTenantAuthQuery extends PageQuery {
    private Long tenantId;
    private Long targetTenantId;
    private Long userId;
    private String status;
}

public class BusinessVisibilityAuth {
    private String token;
    private Long targetTenantId;
    private String dataScope;        // JSON
    private LocalDateTime validUntil;
}

public enum MenuScope {
    ADMIN, BUSINESS, BOTH
}
```

### 1.9 事件定义

```java
package com.xcms.auth.api.event;

public class PermissionChangedEvent implements Serializable {
    private Long userId;
    private Long tenantId;
    private String changeType;    // ROLE_ASSIGNED/ROLE_REMOVED/RULE_CHANGED
}

public class CrossTenantAuthApprovedEvent implements Serializable {
    private Long authId;
    private Long userId;
    private Long tenantId;
    private Long targetTenantId;
    private String token;
    private LocalDateTime validUntil;
}
```

---

## 2. 行级权限实现

### 2.1 实现链路

```
管理员配置规则                          用户查询数据
     │                                     │
     ▼                                     ▼
perm_data_rule                    DataPermissionService
perm_data_rule_role               .getDataPermissionContext(userId, "Order")
     │                                     │
     │                               1. 查用户角色
     │                               2. 查角色关联的规则
     │                               3. 按维度解析规则
     │                               4. 构建 DataPermissionContext
     │                                     │
     │                                     ▼
     │                          DataPermissionSpecBuilder.build(ctx)
     │                          或 DataPermissionQueryDSLBuilder.build(ctx, entity)
     │                                     │
     │                               生成查询条件：
     │                               org_path LIKE '/1/10/%'
     │                               AND created_at >= ?
     │                               AND created_by = ?
     │                                     │
     │                                     ▼
     │                          repository.findAll(dataSpec.and(bizSpec))
     │                                     │
     │                               @TenantId 自动加：
     │                               AND tenant_id = 101
     │                                     │
     ▼                                     ▼
   数据库  ←──────────── SQL 执行 ────────────  返回结果
```

### 2.2 规则配置示例

```sql
-- 规则：销售经理只能看本部门及下级的数据
INSERT INTO perm_data_rule (id, tenant_id, rule_name, rule_type, resource_type,
    dimension, rule_config, priority)
VALUES (1, 101, '销售经理-组织范围', 'ROW', 'Order',
    'ORG', '{"scope":"DEPT_AND_SUB"}', 10);

-- 规则：只能看近3个月数据
INSERT INTO perm_data_rule (id, tenant_id, rule_name, rule_type, resource_type,
    dimension, rule_config, priority)
VALUES (2, 101, '销售经理-时间范围', 'ROW', 'Order',
    'TIME', '{"type":"RELATIVE","value":3,"unit":"MONTHS"}', 20);

-- 规则：只能看自己创建的
INSERT INTO perm_data_rule (id, tenant_id, rule_name, rule_type, resource_type,
    dimension, rule_config, priority)
VALUES (3, 101, '销售经理-仅自己', 'ROW', 'Order',
    'OWNER', '{"field":"created_by"}', 30);

-- 规则绑定到角色5
INSERT INTO perm_data_rule_role (id, tenant_id, rule_id, role_id, scope_value)
VALUES (1, 101, 1, 5, '{"deptPaths":["/1/10/"]}');
INSERT INTO perm_data_rule_role (id, tenant_id, rule_id, role_id, scope_value)
VALUES (2, 101, 2, 5, NULL);
INSERT INTO perm_data_rule_role (id, tenant_id, rule_id, role_id, scope_value)
VALUES (3, 101, 3, 5, NULL);
```

### 2.3 规则维度与生成条件对照

| 维度 | 规则配置 (rule_config) | 角色范围 (scope_value) | 生成的SQL条件 |
|------|----------------------|----------------------|-------------|
| ORG | `{"scope":"DEPT_AND_SUB"}` | `{"deptPaths":["/1/10/"]}` | `org_path LIKE '/1/10/%'` |
| TIME | `{"type":"RELATIVE","value":3,"unit":"MONTHS"}` | 无（规则自身定义） | `created_at >= NOW() - 3 MONTHS` |
| OWNER | `{"field":"created_by"}` | 无 | `created_by = :currentUserId` |
| BUSINESS_LINE | `{"field":"business_line"}` | `{"values":["Electronics","Home"]}` | `business_line IN ('Electronics','Home')` |
| REGION | `{"field":"region"}` | `{"values":["华东","华北"]}` | `region IN ('华东','华北')` |
| TAG | `{"field":"tags","mode":"CONTAINS"}` | `{"values":["VIP","KEY"]}` | `tags LIKE '%VIP%' OR tags LIKE '%KEY%'` |

多维度之间 **AND** 组合，维度内多值 **OR** 组合。

### 2.4 DataPermissionContext 构建（auth-impl）

```java
@Service
public class DataPermissionServiceImpl implements DataPermissionService {

    private final DataRuleRepository ruleRepository;
    private final DataRuleRoleRepository ruleRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    public DataPermissionContext getDataPermissionContext(
            Long userId, String resourceType) {

        Long tenantId = TenantContext.getTenantId();

        // 1. 获取用户的所有角色
        List<Long> roleIds = roleRepository.findRoleIdsByUserId(userId, tenantId);
        if (roleIds.isEmpty()) {
            return DataPermissionContext.empty(userId, tenantId, resourceType);
        }

        // 2. 查询该资源类型下，用户角色关联的所有规则
        List<DataRuleEntity> rules = ruleRepository
            .findActiveRulesByRoles(tenantId, resourceType, roleIds);

        // 3. 查询规则-角色关联（含 scope_value）
        List<DataRuleRoleEntity> ruleRoles = ruleRoleRepository
            .findByRuleIdsAndRoleIds(tenantId,
                rules.stream().map(DataRuleEntity::getId).toList(),
                roleIds);

        // 4. 按维度分组解析规则，构建上下文
        DataPermissionContext.Builder builder = DataPermissionContext.builder()
            .userId(userId)
            .tenantId(tenantId)
            .resourceType(resourceType);

        for (DataRuleEntity rule : rules) {
            String scopeValue = ruleRoles.stream()
                .filter(rr -> rr.getRuleId().equals(rule.getId()))
                .map(DataRuleRoleEntity::getScopeValue)
                .findFirst().orElse(null);

            parseRuleByDimension(rule, scopeValue, userId, builder);
        }

        return builder.build();
    }

    private void parseRuleByDimension(DataRuleEntity rule, String scopeValue,
            Long userId, DataPermissionContext.Builder builder) {

        JsonNode config = JsonUtil.parse(rule.getRuleConfig());
        JsonNode scope = scopeValue != null ? JsonUtil.parse(scopeValue) : null;

        switch (rule.getDimension()) {
            case "ORG"           -> parseOrgRule(scope, builder);
            case "BUSINESS_LINE" -> parseBusinessLineRule(scope, builder);
            case "REGION"        -> parseRegionRule(scope, builder);
            case "TAG"           -> parseTagRule(scope, builder);
            case "TIME"          -> parseTimeRule(config, builder);
            case "OWNER"         -> parseOwnerRule(config, userId, builder);
        }
    }

    private void parseOrgRule(JsonNode scope, DataPermissionContext.Builder builder) {
        if (scope != null && scope.has("deptPaths")) {
            builder.orgPaths(JsonUtil.toStringList(scope.get("deptPaths")));
        }
    }

    private void parseTimeRule(JsonNode config, DataPermissionContext.Builder builder) {
        String type = config.get("type").asText();
        if ("RELATIVE".equals(type)) {
            int value = config.get("value").asInt();
            String unit = config.get("unit").asText();
            LocalDateTime from = switch (unit) {
                case "DAYS"   -> LocalDateTime.now().minusDays(value);
                case "MONTHS" -> LocalDateTime.now().minusMonths(value);
                case "YEARS"  -> LocalDateTime.now().minusYears(value);
                default       -> LocalDateTime.now().minusMonths(value);
            };
            builder.timeFrom(from);
        } else if ("ABSOLUTE".equals(type)) {
            builder.timeFrom(LocalDateTime.parse(config.get("from").asText()));
            builder.timeTo(LocalDateTime.parse(config.get("to").asText()));
        }
    }

    private void parseOwnerRule(JsonNode config, Long userId,
            DataPermissionContext.Builder builder) {
        builder.ownerOnly(true);
        builder.ownerField(config.get("field").asText());  // "created_by"
    }

    // ... 其他维度的解析方法
}
```

### 2.5 查询条件注入

#### 方式 A：JPA Specification

```java
public class DataPermissionSpecBuilder {

    public static <T> Specification<T> build(DataPermissionContext ctx) {
        Specification<T> spec = Specification.where(null);

        if (ctx.hasOrgScope()) {
            spec = spec.and(orgSpec(ctx.getOrgPaths()));
        }
        if (ctx.hasBusinessLineScope()) {
            spec = spec.and(businessLineSpec(ctx.getBusinessLineIds()));
        }
        if (ctx.hasRegionScope()) {
            spec = spec.and(regionSpec(ctx.getRegions()));
        }
        if (ctx.hasTagScope()) {
            spec = spec.and(tagSpec(ctx.getTags()));
        }
        if (ctx.hasTimeScope()) {
            spec = spec.and(timeSpec(ctx.getTimeFrom(), ctx.getTimeTo()));
        }
        if (ctx.hasOwnerScope()) {
            spec = spec.and(ownerSpec(ctx.getUserId(), ctx.getOwnerField()));
        }

        return spec;
    }

    // 组织维度：path LIKE '/1/10/%' OR path LIKE '/1/20/%'
    private static <T> Specification<T> orgSpec(List<String> orgPaths) {
        return (root, query, cb) -> {
            Predicate[] predicates = orgPaths.stream()
                .map(path -> cb.like(root.get("orgPath"), path + "%"))
                .toArray(Predicate[]::new);
            return cb.or(predicates);
        };
    }

    // 时间维度：created_at >= ? AND created_at <= ?
    private static <T> Specification<T> timeSpec(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (from != null) p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null)   p = cb.and(p, cb.lessThanOrEqualTo(root.get("createdAt"), to));
            return p;
        };
    }

    // 归属维度：created_by = :currentUserId
    private static <T> Specification<T> ownerSpec(Long userId, String fieldName) {
        return (root, query, cb) -> cb.equal(root.get(fieldName), userId);
    }

    // ... 其他维度
}
```

#### 方式 B：QueryDSL

```java
public class DataPermissionQueryDSLBuilder {

    public static <T> Predicate build(DataPermissionContext ctx,
            EntityPathBase<T> entity) {
        BooleanBuilder builder = new BooleanBuilder();

        if (ctx.hasOrgScope()) {
            StringPath orgPath = Expressions.stringPath(entity, "orgPath");
            BooleanExpression orgCondition = null;
            for (String path : ctx.getOrgPaths()) {
                orgCondition = orgCondition == null
                    ? orgPath.startsWith(path)
                    : orgCondition.or(orgPath.startsWith(path));
            }
            builder.and(orgCondition);
        }

        if (ctx.hasOwnerScope()) {
            NumberPath<Long> createdBy = Expressions.numberPath(
                Long.class, entity, ctx.getOwnerField());
            builder.and(createdBy.eq(ctx.getUserId()));
        }

        if (ctx.hasTimeScope()) {
            DateTimePath<LocalDateTime> createdAt = Expressions.dateTimePath(
                LocalDateTime.class, entity, "createdAt");
            if (ctx.getTimeFrom() != null) builder.and(createdAt.goe(ctx.getTimeFrom()));
            if (ctx.getTimeTo() != null)   builder.and(createdAt.loe(ctx.getTimeTo()));
        }

        return builder;
    }
}
```

### 2.6 业务模块使用

```java
@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final DataPermissionService dataPermService;

    public PageResult<OrderDTO> findOrders(OrderQuery query, Long userId) {

        // 1. 构建行级权限条件
        DataPermissionContext ctx = dataPermService
            .getDataPermissionContext(userId, "Order");
        Specification<Order> dataSpec = DataPermissionSpecBuilder.build(ctx);

        // 2. 组合业务条件
        Specification<Order> bizSpec = (root, q, cb) -> {
            Predicate p = cb.conjunction();
            if (query.getKeyword() != null)
                p = cb.and(p, cb.like(root.get("orderNo"), "%" + query.getKeyword() + "%"));
            if (query.getStatus() != null)
                p = cb.and(p, cb.equal(root.get("status"), query.getStatus()));
            return p;
        };

        // 3. 合并条件查询（@TenantId 自动加 tenant_id）
        Page<Order> page = orderRepository.findAll(
            dataSpec.and(bizSpec),
            PageRequest.of(query.getPage() - 1, query.getSize())
        );

        // 最终SQL：
        // SELECT * FROM biz_order
        // WHERE order_no LIKE '%keyword%'           ← 业务条件
        //   AND status = 'PENDING'                  ← 业务条件
        //   AND org_path LIKE '/1/10/%'             ← 行级权限-组织
        //   AND created_at >= '2026-04-24'          ← 行级权限-时间
        //   AND created_by = 1001                   ← 行级权限-归属
        //   AND tenant_id = 101                     ← @TenantId 自动

        return PageResult.of(
            page.getContent().stream().map(this::toDTO).toList(),
            page.getTotalElements(),
            query.getPage(), query.getSize()
        );
    }
}
```

### 2.7 QueryDSL 与 @TenantId 兼容性

QueryDSL 的 `JPAQueryFactory` 模式生成 JPQL，经过 Hibernate 解析生成 SQL，`@TenantId` 在 SQL 生成阶段自动注入，因此 **QueryDSL 与 `@TenantId` 完全兼容**。

| 场景 | @TenantId 是否生效 | 说明 |
|------|:---:|------|
| QueryDSL 基本查询 | ✅ | JPQL → Hibernate → SQL，自动加 tenant_id |
| QueryDSL JOIN | ✅ | 主表和关联表都自动加 |
| QueryDSL 子查询 | ✅ | 子查询也自动加 |
| QueryDSL 聚合 (COUNT/SUM) | ✅ | 自动加 |
| QueryDSL DTO 投影 | ✅ | 底层实体查询自动加 |
| QueryDSL 批量 UPDATE/DELETE | ⚠️ | Hibernate 6.4+ 应支持，建议手动加保险 |
| QueryDSL 原生 SQL | ❌ | 需手动加 tenant_id |

**最佳实践**：行级权限用 QueryDSL `BooleanBuilder` 组合数据权限条件，租户隔离由 `@TenantId` 自动处理，两者分层互补：

```java
// QueryDSL + @TenantId + 数据权限 组合
public List<Order> findOrders(Long userId, String keyword) {
    QOrder order = QOrder.order;

    // 1. 数据权限条件（手动组合）
    DataPermissionContext ctx = dataPermService
        .getDataPermissionContext(userId, "Order");
    Predicate dataPerm = DataPermissionQueryDSLBuilder.build(ctx, order);

    // 2. 业务条件（手动组合）
    BooleanBuilder biz = new BooleanBuilder();
    if (keyword != null) {
        biz.and(order.orderNo.contains(keyword));
    }

    // 3. 查询
    return queryFactory
        .selectFrom(order)
        .where(dataPerm, biz)
        // @TenantId 自动加：AND tenant_id = 101
        .fetch();
}
```

---

## 3. 字段级权限实现

### 3.1 实现链路

```
管理员配置脱敏规则                       用户查询数据返回
     │                                     │
     ▼                                     ▼
perm_column_mask                  ResponseBodyAdvice
     │                            （@MaskResource 注解触发）
     │                                     │
     │                               1. 获取当前用户角色
     │                               2. 加载字段级脱敏规则
     │                               3. 遍历 DTO 字段
     │                               4. 按规则脱敏/隐藏
     │                                     │
     ▼                                     ▼
   规则：                                结果：
   phone  → MASK (138****1234)        phone:  138****5678
   idCard → HIDE                      idCard: null
   salary → HIDE                      salary: null
```

### 3.2 规则配置示例

```sql
-- 规则：普通角色看客户信息时，手机号脱敏、身份证隐藏、薪资隐藏
INSERT INTO perm_column_mask (id, tenant_id, resource_type, field_name,
    mask_type, mask_rule, role_ids, status)
VALUES (1, 101, 'Customer', 'phone',
    'MASK', '138****1234', '[6,7,8]', 'ACTIVE');   -- 保留前3后4

INSERT INTO perm_column_mask (id, tenant_id, resource_type, field_name,
    mask_type, mask_rule, role_ids, status)
VALUES (2, 101, 'Customer', 'idCard',
    'HIDE', NULL, '[6,7,8]', 'ACTIVE');             -- 完全隐藏

INSERT INTO perm_column_mask (id, tenant_id, resource_type, field_name,
    mask_type, mask_rule, role_ids, status)
VALUES (3, 101, 'Customer', 'salary',
    'HIDE', NULL, '[6,7]', 'ACTIVE');               -- 完全隐藏

-- 管理员角色不配置规则 → 不受限制
```

### 3.3 脱敏类型说明

| mask_type | 说明 | 规则示例 | 效果 |
|-----------|------|---------|------|
| HIDE | 字段不返回（设为null） | 无 | `salary: null` |
| MASK | 字段脱敏 | `138****1234` | `phone: 138****5678` |
| PARTIAL | 部分可见 | `1101**********34` | `idCard: 1101**********34` |

### 3.4 规则加载（auth-impl）

```java
@Service
public class ColumnMaskServiceImpl implements ColumnMaskService {

    private final ColumnMaskRepository maskRepository;
    private final RoleRepository roleRepository;

    /**
     * 获取用户对指定资源类型的所有列级脱敏规则
     */
    public List<ColumnMaskRule> getMaskRules(Long userId, String resourceType) {
        Long tenantId = TenantContext.getTenantId();

        // 1. 获取用户角色
        List<Long> roleIds = roleRepository.findRoleIdsByUserId(userId, tenantId);
        if (roleIds.isEmpty()) return List.of();

        // 2. 查询该资源类型的所有规则
        List<ColumnMaskEntity> allRules = maskRepository
            .findByResourceTypeAndTenant(tenantId, resourceType);

        // 3. 过滤：只返回适用于用户角色的规则
        return allRules.stream()
            .filter(rule -> {
                List<Long> ruleRoleIds = JsonUtil.toLongList(rule.getRoleIds());
                return roleIds.stream().anyMatch(ruleRoleIds::contains);
            })
            .map(this::toRule)
            .toList();
    }

    private ColumnMaskRule toRule(ColumnMaskEntity entity) {
        return new ColumnMaskRule(
            entity.getFieldName(),
            MaskType.valueOf(entity.getMaskType()),
            entity.getMaskRule()
        );
    }
}

public record ColumnMaskRule(String fieldName, MaskType maskType, String maskRule) {}

public enum MaskType { HIDE, MASK, PARTIAL }
```

### 3.5 脱敏执行

```java
@Service
public class ColumnMaskServiceImpl implements ColumnMaskService {

    /**
     * 对单个 DTO 应用脱敏
     */
    @Override
    public <T> void apply(T dto, Long userId, String resourceType) {
        List<ColumnMaskRule> rules = getMaskRules(userId, resourceType);
        if (rules.isEmpty()) return;
        applyRules(dto, rules);
    }

    /**
     * 批量脱敏
     */
    @Override
    public <T> void applyBatch(List<T> dtos, Long userId, String resourceType) {
        if (dtos.isEmpty()) return;
        List<ColumnMaskRule> rules = getMaskRules(userId, resourceType);
        if (rules.isEmpty()) return;
        for (T dto : dtos) applyRules(dto, rules);
    }

    /**
     * 核心脱敏逻辑：反射修改字段值
     */
    private <T> void applyRules(T dto, List<ColumnMaskRule> rules) {
        for (ColumnMaskRule rule : rules) {
            try {
                Field field = dto.getClass().getDeclaredField(rule.fieldName());
                field.setAccessible(true);
                Object value = field.get(dto);
                if (value == null) continue;

                Object masked = switch (rule.maskType()) {
                    case HIDE    -> null;
                    case MASK    -> maskValue(value, rule.maskRule());
                    case PARTIAL -> partialValue(value, rule.maskRule());
                };
                field.set(dto, masked);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // 字段不存在，跳过
            }
        }
    }

    /**
     * 脱敏：规则格式 138****1234（保留前3后4）
     */
    private Object maskValue(Object value, String maskRule) {
        String str = value.toString();
        if (maskRule == null) return "****";

        // 解析规则中的保留前缀和后缀长度
        int starStart = maskRule.indexOf('*');
        int starEnd = maskRule.lastIndexOf('*');
        int prefixLen = starStart;
        int suffixLen = maskRule.length() - starEnd - 1;

        if (str.length() <= prefixLen + suffixLen) return str;

        return str.substring(0, prefixLen)
            + "*".repeat(str.length() - prefixLen - suffixLen)
            + (suffixLen > 0 ? str.substring(str.length() - suffixLen) : "");
    }

    /**
     * 部分可见：规则格式 1101**********34（保留前4后2）
     */
    private Object partialValue(Object value, String maskRule) {
        String str = value.toString();
        if (str.length() <= 6) return "******";
        return str.substring(0, 4)
            + "*".repeat(str.length() - 6)
            + str.substring(str.length() - 2);
    }
}
```

### 3.6 自动脱敏（ResponseBodyAdvice，推荐）

用 Spring 的 `ResponseBodyAdvice` 在响应返回前自动脱敏，业务代码零侵入：

```java
@RestControllerAdvice
public class ColumnMaskResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ColumnMaskService columnMaskService;

    @Override
    public boolean supports(MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        // 只处理标注了 @MaskResource 的方法
        return returnType.hasMethodAnnotation(MaskResource.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {

        if (body == null) return null;

        MaskResource annotation = returnType.getMethodAnnotation(MaskResource.class);
        String resourceType = annotation.value();
        Long userId = TenantContext.getCurrentUserId();

        if (body instanceof PageResult<?> pageResult) {
            columnMaskService.applyBatch(pageResult.getList(), userId, resourceType);
        } else if (body instanceof List<?> list) {
            columnMaskService.applyBatch(list, userId, resourceType);
        } else if (body instanceof ApiResponse<?> apiResponse && apiResponse.getData() != null) {
            Object data = apiResponse.getData();
            if (data instanceof List<?> list) {
                columnMaskService.applyBatch(list, userId, resourceType);
            } else {
                columnMaskService.apply(data, userId, resourceType);
            }
        } else {
            columnMaskService.apply(body, userId, resourceType);
        }

        return body;
    }
}

/** 方法注解：标记需要自动脱敏的接口 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskResource {
    String value();  // 资源类型，如 "Customer"
}
```

### 3.7 Controller 使用

```java
@RestController
public class CustomerController {

    @GetMapping("/api/customers/{id}")
    @MaskResource("Customer")    // 加注解，自动脱敏
    public ApiResponse<CustomerDTO> getCustomer(@PathVariable Long id) {
        CustomerDTO dto = customerService.getCustomer(id);
        return ApiResponse.success(dto);
        // 返回前自动脱敏，业务代码无感知
    }

    @GetMapping("/api/customers")
    @MaskResource("Customer")
    public ApiResponse<PageResult<CustomerDTO>> listCustomers(CustomerQuery query) {
        PageResult<CustomerDTO> result = customerService.listCustomers(query);
        return ApiResponse.success(result);
    }
}
```

效果：

```json
// 管理员角色（无脱敏规则）看到的：
{ "phone": "13812345678", "idCard": "110101199001011234", "salary": 15000 }

// 普通角色（有脱敏规则）看到的：
{ "phone": "138****5678", "idCard": null, "salary": null }
```

---

## 4. 行级 + 字段级权限协同

完整的一次查询请求：

```
用户请求 GET /api/customers?keyword=张
    │
    ▼
┌─────────────────────────────────────────────┐
│  1. 请求拦截器                                 │
│     识别用户 → 设置 TenantContext             │
│     @TenantId 准备自动注入 tenant_id          │
└────────────────────┬────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│  2. CustomerService.listCustomers()          │
│                                             │
│  2a. 行级权限：                               │
│      DataPermissionContext ctx =             │
│        dataPermService.getContext(userId,    │
│           "Customer")                        │
│      → org_path LIKE '/1/10/%'              │
│      → created_at >= 3个月前                 │
│      → created_by = 当前用户                 │
│                                             │
│  2b. 查询（@TenantId 自动加 tenant_id）：     │
│      WHERE name LIKE '%张%'         ← 业务   │
│        AND org_path LIKE '/1/10/%' ← 行级   │
│        AND created_at >= ?         ← 行级   │
│        AND created_by = ?          ← 行级   │
│        AND tenant_id = 101         ← @TenantId│
│                                             │
│  2c. 返回 DTO 列表                            │
└────────────────────┬────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│  3. ResponseBodyAdvice（@MaskResource）      │
│                                             │
│  3a. 加载字段级规则：                          │
│      用户角色 → phone: MASK                  │
│                idCard: HIDE                  │
│                salary: HIDE                  │
│                                             │
│  3b. 对每个 DTO 脱敏：                         │
│      phone: 13812345678 → 138****5678       │
│      idCard: 110101... → null               │
│      salary: 15000 → null                   │
└────────────────────┬────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│  4. 返回 JSON                                 │
│  {                                          │
│    "list": [                                │
│      { "id": 1, "name": "张三",             │
│        "phone": "138****5678",              │
│        "idCard": null,                      │
│        "salary": null }                     │
│    ], "total": 1 }                          │
└─────────────────────────────────────────────┘
```

### 三层权限总结

| 层级 | 机制 | 生效时机 | 实现方式 | 业务侵入 |
|------|------|---------|---------|:---:|
| 租户隔离 | `@TenantId` | SQL 生成阶段 | Hibernate 自动注入 | 零 |
| 行级权限 | DataPermissionContext + Specification/QueryDSL | 查询 WHERE 条件 | 手动组合条件 | 中（需调用） |
| 字段级权限 | ColumnMaskService + ResponseBodyAdvice | 查询结果返回后 | 反射脱敏 / 注解自动 | 低（注解） |

---

## 5. DDL

参见 [db/ddl/03-authorization.sql](../../db/ddl/03-authorization.sql)
