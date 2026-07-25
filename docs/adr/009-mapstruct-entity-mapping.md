# ADR-009: 使用 MapStruct 进行 Entity ↔ DTO 转换

- **Status**: Accepted
- **Date**: 2026-07-24

## Context

XCMS 采用 API/Impl 模块拆分模式，Entity（JPA 实体）定义在 impl 模块中不对外暴露，DTO 定义在 api 模块中作为接口契约。需要选择 Entity ↔ DTO 的转换方案。

要求：
- 类型安全（编译期检查）
- 高性能（避免运行时反射）
- 支持复杂映射（字段名不同、嵌套对象、类型转换）
- 与 JPA / Lombok / 字段级权限兼容

## Decision

采用 **MapStruct** 作为 Entity ↔ DTO 转换框架。

- Mapper 接口定义在 `xxx-impl/mapper/` 目录
- 使用 `@Mapper(componentModel = "spring")` 声明为 Spring Bean
- 编译期生成实现代码，零反射

## Alternatives

### 方案 B：手写 Mapper

- **优点**：最灵活、性能最好
- **缺点**：代码量大、重复劳动、容易遗漏字段

### 方案 C：BeanUtils / ModelMapper

- **优点**：零配置
- **缺点**：运行时反射性能差、无编译期检查、复杂映射不支持、JPA 懒加载问题

## Consequences

### 正面

- 编译期生成代码，性能最优（与手写相当）
- 类型安全，字段变更编译期即可发现
- 声明式接口，代码量少
- 支持自定义转换方法、嵌套映射、忽略字段
- 与 JPA 懒加载兼容（在 Service 层 fetch join 后再映射）
- 与字段级权限兼容（先转换再脱敏）
- 与 Lombok 兼容（需配置 annotation processor 顺序）

### 负面

- 需要 annotation processor 配置
- 与 Lombok 同时使用时需注意 processor 顺序

### 使用规范

1. Entity 永远不暴露到 API 层
2. 敏感字段（password 等）用 `ignore = true` 忽略
3. `@TenantId` 字段在创建时用 `ignore = true`（Hibernate 自动填充）
4. 时间戳字段（createdAt/updatedAt）用 `ignore = true`（@PrePersist 填充）
5. 字段级脱敏在 MapStruct 转换之后执行
