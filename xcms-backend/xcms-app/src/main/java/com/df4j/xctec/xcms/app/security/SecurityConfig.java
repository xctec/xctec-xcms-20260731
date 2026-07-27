package com.df4j.xctec.xcms.app.security;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * 应用安全底座（ADR-016）：SecurityFilterChain + JWT Resource Server。
 *
 * <p>此前认证由 Portal 的 TenantInterceptor（MVC 拦截器）承担，属于框架错位：
 * 拦截器晚于 Servlet Filter、无法覆盖非 MVC 端点、且无声明式鉴权能力。
 * 本配置将「认证 + 声明式授权」上收至 Servlet Filter 层：</p>
 * <ul>
 *   <li>JWT 校验：HS256 对称密钥，与 identity 模块 JwtTokenProvider 同源
 *       （xcms.identity.jwt.secret），登录签发 ↔ 网关校验语义一致；</li>
 *   <li>权限装载：{@link JwtPermissionAuthenticationConverter} 实时查询权限服务
 *       （带缓存），支撑 @PreAuthorize 方法级鉴权（@EnableMethodSecurity）；</li>
 *   <li>401/403 输出与 ApiResponse 错误结构统一。</li>
 * </ul>
 *
 * <p>TenantInterceptor 退化为纯租户上下文填充器（AT-07），不再承担鉴权。</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 免认证端点白名单，与 Portal WebConfig 的拦截器排除路径保持一致。
     */
    private static final String[] PERMIT_ALL = {
            "/api/auth/login", "/api/auth/refresh",
            "/api/tenant/lookup",
            "/api/sso/authorize", "/api/sso/callback",
            "/actuator/**",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**",
            "/error", "/favicon.ico",
            "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   PermissionService permissionService,
                                                   ObjectMapper objectMapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // h2-console 控制台需要 iframe 同源
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PERMIT_ALL).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                new JwtPermissionAuthenticationConverter(permissionService)))
                        .authenticationEntryPoint(RestSecurityHandlers.authenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(RestSecurityHandlers.accessDeniedHandler(objectMapper)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(RestSecurityHandlers.authenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(RestSecurityHandlers.accessDeniedHandler(objectMapper)));
        return http.build();
    }

    /**
     * 与 {@code JwtTokenProvider} 同一 HS256 对称密钥：登录签发的 access token
     * 可直接由 Resource Server 校验，无需引入独立认证服务器。
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${xcms.identity.jwt.secret:xcms-jwt-default-secret-key-2026-07-25!}") String secret) {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
