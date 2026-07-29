package com.df4j.xctec.xcms.web.security;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.web.config.XcmsJwtProperties;
import com.df4j.xctec.xcms.web.config.XcmsSecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
 * <p>原位于 xcms-app（单体聚合模块），现抽离为 web-starter 的自动配置，
 * 单体与未来的微服务均可复用同一套「认证 + 声明式授权」底座。</p>
 *
 * <ul>
 *   <li>免认证路径通过 {@code xcms.security.permit-paths} 配置（保留原默认值）；</li>
 *   <li>JWT 校验密钥通过 {@code xcms.jwt.secret} 配置，与 identity 模块
 *       {@code xcms.identity.jwt.secret} 同源（默认均取 ${XCMS_JWT_SECRET}）；</li>
 *   <li>权限装载由 {@code JwtPermissionAuthenticationConverter} 实时查询权限服务，
 *       支撑 @PreAuthorize（@EnableMethodSecurity）；</li>
 *   <li>401/403 输出与 ApiResponse 错误结构统一。</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({XcmsSecurityProperties.class, XcmsJwtProperties.class})
public class SecurityConfig {

    private final XcmsSecurityProperties securityProperties;

    public SecurityConfig(XcmsSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

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
                        .requestMatchers(securityProperties.getPermitPaths().toArray(new String[0])).permitAll()
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
    public JwtDecoder jwtDecoder(XcmsJwtProperties jwtProperties) {
        SecretKey key = new SecretKeySpec(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
