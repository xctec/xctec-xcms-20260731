# ADR-008: JPA @TenantId 多租户

- **Status**: Accepted
- **Date**: 2026-07-24

## Context

XCMS 使用 JPA 作为 ORM，需要实现多租户数据隔离。需要选择 JPA 下的多租户实现方式。

## Decision

采用 **Hibernate 7 原生 `@TenantId` 注解** 实现多租户隔离。

- 所有需要租户隔离的实体继承 `TenantEntity` 基类
- 基类标注 `@TenantId`，Hibernate 自动处理查询过滤和写入填充
- 业务代码零感知，无需手动处理 tenant_id

```java
@MappedSuperclass
public abstract class TenantEntity {
    @TenantId
    @Column(name = "tenant_id")
    private Long tenantId;
}
```

配合 `TenantContext`（ThreadLocal）传递当前租户，`CurrentTenantIdentifierResolver` 告诉 Hibernate 当前租户。

## Alternatives

### 方案 B：Hibernate @Filter

- **优点**：兼容旧版本
- **缺点**：需要手动启用 Filter、INSERT 需额外处理、代码量更多

### 方案 C：MyBatis 拦截器

- **优点**：SQL 完全可控
- **缺点**：需要手写拦截器、复杂查询更灵活但简单场景代码量大

## Consequences

### 正面

- `@TenantId` 一个注解搞定，零代码
- Hibernate 7 原生支持，稳定可靠
- 业务代码完全无感知
- 与 Spring Boot 4 + Hibernate 7 技术栈一致

### 负面

- 复杂查询需要用 `Specification` 组合，比直接写 SQL 稍繁琐
- 需要注意 N+1 问题

### 缓解措施

- 数据权限用 `Specification` 或 `QueryDSL` 动态组合
- 复杂报表用原生 SQL（`@Query(nativeQuery=true)`）
- 注意懒加载和 N+1 问题
- QueryDSL 与 `@TenantId` 完全兼容（JPQL → Hibernate → SQL，自动注入 tenant_id），但原生 SQL 模式需手动加 tenant_id 条件
- 批量 UPDATE/DELETE 建议手动加 tenant_id 作为保险
