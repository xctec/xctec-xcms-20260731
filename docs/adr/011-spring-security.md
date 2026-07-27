# ADR-011: 鉴权体系迁移 Spring Security

- **Status**: Accepted
- **Date**: 2026-07-27

## Context

当前鉴权自研：`TenantInterceptor`（HandlerInterceptor）+ `JwtTokenProvider` + `JwtTenantResolver` 承担 token 校验与租户解析，方法级权限缺失，`@PreAuthorize` 不可用，权限判断散落且强依赖前端 `<Can>` 兜底。仅引入 `spring-security-crypto`（`PasswordEncoder`），未启用 Spring Security 过滤链。

自研链路可行但缺少成熟生态兜底：方法级权限、CSRF、OAuth2 Resource Server、安全漏洞修复等都需团队自制，长期维护成本与安全隐患高（复盘 M1）。

## Decision

迁移到 **Spring Security 6 Resource Server（JWT）**，鉴权与租户解析职责分离：

1. `SecurityFilterChain` 配 `oauth2ResourceServer().jwt()`，由 Spring Security 校验 JWT 签名/exp/claims，替代 `TenantInterceptor` 的 token 校验职责。
2. `TenantInterceptor` 退化为仅"从已认证 `Authentication` 提取 tenantId/userId 写入 `TenantContext`"，不再做 token 解析与 401 响应。
3. 启用 `@EnableMethodSecurity` + `@PreAuthorize("hasAuthority('user:create')")`，后端强制校验权限，不依赖前端 `<Can>`。
4. `JwtAuthenticationConverter` 从 JWT claim 构建 `GrantedAuthority`（权限码或角色），配合 `PermissionService` 缓存。
5. 登录/SSO 仍由 `AuthController`/`SsoController` 发 token，校验侧统一由 Spring Security。
6. 保留 `PasswordEncoder`（已在用）。

## Alternatives

### 方案 B：继续自研鉴权

- **优点**：零迁移成本，现有代码不动
- **缺点**：方法级权限/CSRF/OAuth2 生态全靠自制，安全漏洞需自行跟进，长期成本高

### 方案 C：Shiro

- **优点**：轻量，学习成本低
- **缺点**：生态不如 Spring Security，与 Spring Boot 整合需额外适配，社区活跃度下降

## Consequences

### 正面

- 方法级权限 `@PreAuthorize` 声明式校验，后端强制，安全基线提升
- 复用 Spring Security 生态（OAuth2 RS、CSRF、安全更新）
- 为服务令牌（service account JWT，见 ADR-012）提供统一签发/校验基础
- 401/403 响应统一由 `AuthenticationEntryPoint`/`AccessDeniedHandler` 处理，契约一致

### 负面

- 迁移期需保持 `TenantInterceptor` 兼容（租户解析不能一步切走）
- 方法级注解需逐 controller 补，工作量大，可分批
- 团队需熟悉 Spring Security 配置

### 缓解措施

- 迁移分两步：先切 token 校验到 Spring Security（拦截器退化），再逐模块补 `@PreAuthorize`
- 保留 `PermissionService` 作为权限来源，`JwtAuthenticationConverter` 复用其缓存
