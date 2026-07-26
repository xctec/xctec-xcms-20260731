package com.df4j.xctec.xcms.identity.api.tenant;

import java.util.Optional;

/**
 * 租户解析器（SPI）。
 *
 * <p>从请求凭证（如 JWT）中解析出当前请求的租户与用户标识。
 * 实现由 identity-impl 提供（基于 JWT），接口定义在 identity-api，
 * 使 Portal 等调用方仅依赖接口、不耦合 identity 内部实现。</p>
 */
public interface TenantResolver {

    /**
     * 解析给定的 Bearer token。
     *
     * @param token 已去除 "Bearer " 前缀的 token 字符串
     * @return 解析成功返回租户与用户信息；token 缺失/无效/过期返回 empty
     */
    Optional<ResolvedTenant> resolve(String token);
}
