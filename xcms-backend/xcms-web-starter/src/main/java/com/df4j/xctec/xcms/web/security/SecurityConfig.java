package com.df4j.xctec.xcms.web.security;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.web.config.XcmsJwtProperties;
import com.df4j.xctec.xcms.web.config.XcmsSecurityProperties;
import com.df4j.xctec.xcms.web.config.XcmsWebProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 应用安全底座（ADR-016）：SecurityFilterChain + JWT Resource Server。
 *
 * <p>原位于 xcms-app（单体聚合模块），现抽离为 web-starter 的自动配置，
 * 单体与未来的微服务均可复用同一套「认证 + 声明式授权」底座。</p>
 *
 * <ul>
 *   <li>免认证路径通过 {@code xcms.security.permit-paths} 配置（保留原默认值）；</li>
 *   <li>CORS 通过 {@code xcms.web.cors.allowed-origins} 配置（见 {@link #corsConfigurationSource}）；</li>
 *   <li>JWT 校验密钥通过 {@code xcms.jwt.secret} 配置，与 identity 模块
 *       {@code xcms.identity.jwt.secret} 同源（默认均取 ${XCMS_JWT_SECRET}）；</li>
 *   <li>权限装载由 {@link JwtPermissionAuthenticationConverter} 实时查询权限服务，
 *       支撑 @PreAuthorize（@EnableMethodSecurity）；</li>
 *   <li>401/403 输出与 ApiResponse 错误结构统一；</li>
 *   <li>可通过 {@code xcms.security.enabled=false} 整体关闭本安全自动配置（仅留 Web/OpenAPI）。</li>
 * </ul>
 *
 * <p><b>分层说明（评审 P1-1）：</b>starter 仍编译依赖 auth-api（接口层，无业务实现），
 * 但运行时不再强依赖 {@link PermissionService} Bean：检测到该 Bean 时使用
 * {@link JwtPermissionAuthenticationConverter} 装载权限，否则回退到 Spring 默认
 * {@link JwtAuthenticationConverter}（仅解析 JWT 内置声明）。因此「任意服务依赖即获得
 * 全套能力」成立——未提供 PermissionService 的服务也能正常启动（@PreAuthorize 退化为无权限）。</p>
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "xcms.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({XcmsSecurityProperties.class, XcmsJwtProperties.class, XcmsWebProperties.class})
public class SecurityConfig {

    private final XcmsSecurityProperties securityProperties;

    public SecurityConfig(XcmsSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   ObjectMapper objectMapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // h2-console 控制台需要 iframe 同源
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(securityProperties.getPermitPaths().toArray(new String[0])).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(RestSecurityHandlers.authenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(RestSecurityHandlers.accessDeniedHandler(objectMapper)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(RestSecurityHandlers.authenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(RestSecurityHandlers.accessDeniedHandler(objectMapper)));
        return http.build();
    }

    /**
     * JWT → Authentication 转换器。
     *
     * <p>检测到 {@link PermissionService} Bean 时使用 {@link JwtPermissionAuthenticationConverter}
     * （实时装载用户权限，支撑 @PreAuthorize）；否则回退到 Spring 默认
     * {@link JwtAuthenticationConverter}（仅解析 JWT 内置 scope/roles 声明），保证无权限
     * 服务的 SecurityFilterChain 仍能装配、应用正常启动。</p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "jwtAuthenticationConverter")
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter(
            ObjectProvider<PermissionService> permissionServiceProvider) {
        PermissionService permissionService = permissionServiceProvider.getIfAvailable();
        if (permissionService != null) {
            return new JwtPermissionAuthenticationConverter(permissionService);
        }
        log.warn("未检测到 PermissionService Bean，JwtPermissionAuthenticationConverter 不可用，"
                + " 回退到默认 JwtAuthenticationConverter（@PreAuthorize 权限声明将不生效，仅解析 JWT 内置声明）。");
        return new JwtAuthenticationConverter();
    }

    /**
     * CORS 配置源（评审 P1-2）：与 Security 层的 {@code .cors().configurationSource(...)} 打通，
     * 替代原 xcms-app 的 {@code CorsConfig}（走 WebMvcConfigurer.addCorsMappings，与 Security 不通）。
     *
     * <p>allowed-origins 含 {@code *} 时改用 allowedOriginPatterns，避免
     * allowCredentials=true 与 allowedOrigins=* 的非法组合（Spring 会抛 IllegalArgumentException）。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public CorsConfigurationSource corsConfigurationSource(XcmsWebProperties webProperties) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = webProperties.getCors().getAllowedOrigins();
        if (origins != null && origins.contains("*")) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else if (origins != null) {
            config.setAllowedOrigins(origins);
        } else {
            config.setAllowedOriginPatterns(List.of("*"));
        }
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
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
