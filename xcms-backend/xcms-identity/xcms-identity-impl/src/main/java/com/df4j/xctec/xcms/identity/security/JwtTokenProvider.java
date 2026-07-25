package com.df4j.xctec.xcms.identity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌生成与解析工具。
 *
 * <p>使用 JJWT 0.12.x 生成 HS256 签名令牌，替代原先的 UUID 随机串，
 * 使 Token 可自包含 userId/tenantId/username 并自带过期时间，便于无状态校验。</p>
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtTokenProvider(
            @Value("${xcms.identity.jwt.secret:xcms-jwt-default-secret-key-2026-07-25!}") String secret,
            @Value("${xcms.identity.jwt.access-ttl-seconds:7200}") long accessTtlSeconds,
            @Value("${xcms.identity.jwt.refresh-ttl-seconds:604800}") long refreshTtlSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public String generateAccessToken(Long userId, Long tenantId, String username) {
        return buildToken(userId, tenantId, username, accessTtlSeconds);
    }

    public String generateRefreshToken(Long userId, Long tenantId, String username) {
        return buildToken(userId, tenantId, username, refreshTtlSeconds);
    }

    private String buildToken(Long userId, Long tenantId, String username, long ttlSeconds) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ttlSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("tenantId", tenantId)
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验签名与过期时间，返回声明载荷。
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
