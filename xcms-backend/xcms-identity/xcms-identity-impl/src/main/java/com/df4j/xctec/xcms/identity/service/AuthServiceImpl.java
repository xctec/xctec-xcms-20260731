package com.df4j.xctec.xcms.identity.service;

import com.df4j.xctec.xcms.identity.api.AuthService;
import com.df4j.xctec.xcms.identity.api.dto.LoginRequest;
import com.df4j.xctec.xcms.identity.api.dto.LoginResult;
import com.df4j.xctec.xcms.identity.api.dto.SessionDTO;
import com.df4j.xctec.xcms.identity.api.dto.TokenInfo;
import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.identity.api.event.UserLoginEvent;
import com.df4j.xctec.xcms.identity.domain.User;
import com.df4j.xctec.xcms.identity.domain.UserSession;
import com.df4j.xctec.xcms.identity.mapper.UserMapper;
import com.df4j.xctec.xcms.identity.repository.UserRepository;
import com.df4j.xctec.xcms.identity.repository.UserSessionRepository;
import com.df4j.xctec.xcms.identity.security.JwtTokenProvider;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.identity.domain.SsoBinding;
import com.df4j.xctec.xcms.identity.domain.SsoProvider;
import com.df4j.xctec.xcms.identity.repository.SsoBindingRepository;
import com.df4j.xctec.xcms.identity.repository.SsoProviderRepository;
import com.df4j.xctec.xcms.identity.api.RoleService;
import com.df4j.xctec.xcms.identity.api.SsoService;
import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final long SESSION_TTL_SECONDS = 7200L;
    private static final int MAX_LOGIN_FAIL_ATTEMPTS = 5;
    private static final int LOGIN_LOCK_MINUTES = 30;

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final UserMapper userMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final DomainEventPublisher eventPublisher;
    private final SsoProviderRepository ssoProviderRepository;
    private final SsoBindingRepository ssoBindingRepository;
    private final SsoService ssoService;
    private final RoleService roleService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Lazy
    @Autowired
    private AuthServiceImpl self;

    @Override
    public LoginResult login(LoginRequest request) {
        // 登录端点被 TenantInterceptor 排除（无 token），需在进入事务前确定并设置租户上下文：
        // @TenantId 的当前租户由 Hibernate 会话在开启时经 CurrentTenantIdentifierResolver 捕获，
        // 会话开启后中途 set 无法改变会话租户。故拆为非事务入口设置上下文 + 经代理调用 @Transactional doLogin。
        Long tenantId = request.getTenantId() != null ? request.getTenantId() : TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "无法确定租户");
        }
        TenantContext.set(tenantId);
        try {
            return self.doLogin(request, tenantId);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public LoginResult doLogin(LoginRequest request, Long tenantId) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS));
        LocalDateTime now = LocalDateTime.now();
        // 锁定检查：处于锁定时间内禁止登录
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(now)) {
            throw new BusinessException("1007", "账号已被锁定，请于锁定时间结束后重试");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 记录失败次数，连续失败达到阈值则锁定 30 分钟
            user.setLoginFailCount(user.getLoginFailCount() + 1);
            if (user.getLoginFailCount() >= MAX_LOGIN_FAIL_ATTEMPTS) {
                user.setLockUntil(now.plusMinutes(LOGIN_LOCK_MINUTES));
                user.setStatus(UserStatus.LOCKED);
            }
            userRepository.save(user);
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS);
        }
        // 登录成功：清零失败计数并解除锁定
        user.setLoginFailCount(0);
        user.setLockUntil(null);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCodes.AUTH_ACCOUNT_DISABLED);
        }
        String loginIp = resolveClientIp();
        UserSession session = UserSession.builder()
                .userId(user.getId())
                .token(jwtTokenProvider.generateAccessToken(user.getId(), tenantId, user.getUsername()))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user.getId(), tenantId, user.getUsername()))
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .deviceType(request.getDeviceType())
                .loginIp(loginIp)
                .loginAt(now)
                .expireAt(now.plusSeconds(SESSION_TTL_SECONDS))
                .lastActiveAt(now)
                .status("ACTIVE")
                .build();
        sessionRepository.save(session);

        user.setLastLoginAt(now);
        user.setLastLoginIp(loginIp);
        userRepository.save(user);

        UserLoginEvent loginEvent = new UserLoginEvent();
        loginEvent.setUserId(user.getId());
        loginEvent.setTenantId(tenantId);
        loginEvent.setIp(loginIp);
        loginEvent.setDeviceType(request.getDeviceType());
        loginEvent.setLoginAt(now);
        loginEvent.setSuccess(true);
        eventPublisher.publish(loginEvent);

        LoginResult result = new LoginResult();
        result.setToken(session.getToken());
        result.setRefreshToken(session.getRefreshToken());
        result.setExpiresIn(SESSION_TTL_SECONDS);
        result.setUser(userMapper.toDTO(user));
        result.setForceChangePassword(user.getPasswordChangedAt() == null);
        return result;
    }

    @Override
    @Transactional
    public LoginResult ssoCallback(String code, String state) {
        // 1. state 三段式：tenantId:serverCode:csrfToken，校验并解析租户（防 CSRF/重放）
        if (!StringUtils.hasText(state)) {
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "缺少 SSO state");
        }
        String[] stateParts = state.split(":", 3);
        if (stateParts.length != 3 || !StringUtils.hasText(stateParts[0])
                || !StringUtils.hasText(stateParts[1]) || !StringUtils.hasText(stateParts[2])) {
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "非法的 SSO state");
        }
        Long tenantId = Long.valueOf(stateParts[0].trim());
        String serverCode = stateParts[1];
        ssoService.validateSsoState(serverCode, stateParts[2]);
        TenantContext.set(tenantId);

        SsoProvider p = ssoProviderRepository.findByServerCodeAndEnabled(serverCode, true)
                .orElseThrow(() -> new BusinessException(ErrorCodes.BUSINESS_ERROR, "SSO 服务不可用: " + serverCode));
        if (!"OAUTH2".equals(p.getProtocol()) && !"OIDC".equals(p.getProtocol())) {
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "暂不支持的 SSO 协议: " + p.getProtocol());
        }
        String accessToken = exchangeToken(p, code);
        JsonNode ui = fetchUserInfo(p, accessToken);
        String idpUserIdField = p.getIdpUserIdField() != null ? p.getIdpUserIdField() : "sub";
        String usernameField = p.getUsernameField() != null ? p.getUsernameField() : "email";
        String emailField = p.getEmailField() != null ? p.getEmailField() : "email";
        String nameField = p.getNameField() != null ? p.getNameField() : "name";

        String idpOpenId = ui.path(idpUserIdField).asText();
        String username = ui.path(usernameField).asText();
        String email = ui.hasNonNull(emailField) ? ui.path(emailField).asText() : null;
        String name = ui.hasNonNull(nameField) ? ui.path(nameField).asText() : null;
        if (!StringUtils.hasText(username)) {
            username = idpOpenId;
        }

        User user = ssoBindingRepository.findByProviderIdAndIdpOpenId(p.getId(), idpOpenId)
                .flatMap(b -> userRepository.findById(b.getUserId())).orElse(null);
        if (user == null) {
            if (!p.isAutoCreate()) {
                throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "未找到 SSO 绑定账号且未开启自动创建");
            }
            user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .realName(name)
                    .email(email)
                    .status(UserStatus.ACTIVE)
                    .userType("SSO")
                    .build();
            user.setTenantId(tenantId);
            user = userRepository.save(user);
            if (p.getDefaultRole() != null) {
                roleService.assignRoleToUser(user.getId(), p.getDefaultRole(), RoleScope.TENANT, String.valueOf(tenantId));
            }
        }

        SsoBinding binding = ssoBindingRepository.findByProviderIdAndIdpOpenId(p.getId(), idpOpenId).orElse(null);
        if (binding == null) {
            binding = SsoBinding.builder()
                    .providerId(p.getId()).userId(user.getId()).idpOpenId(idpOpenId)
                    .idpUsername(username).build();
        }
        binding.setLastLoginAt(LocalDateTime.now());
        ssoBindingRepository.save(binding);

        LocalDateTime now = LocalDateTime.now();
        String loginIp = resolveClientIp();
        UserSession session = UserSession.builder()
                .userId(user.getId())
                .token(jwtTokenProvider.generateAccessToken(user.getId(), tenantId, user.getUsername()))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user.getId(), tenantId, user.getUsername()))
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .deviceType("SSO")
                .loginIp(loginIp)
                .loginAt(now)
                .expireAt(now.plusSeconds(SESSION_TTL_SECONDS))
                .lastActiveAt(now)
                .status("ACTIVE")
                .build();
        session.setTenantId(tenantId);
        sessionRepository.save(session);

        user.setLastLoginAt(now);
        user.setLastLoginIp(loginIp);
        userRepository.save(user);

        UserLoginEvent loginEvent = new UserLoginEvent();
        loginEvent.setUserId(user.getId());
        loginEvent.setTenantId(tenantId);
        loginEvent.setIp(loginIp);
        loginEvent.setDeviceType("SSO");
        loginEvent.setLoginAt(now);
        loginEvent.setSuccess(true);
        eventPublisher.publish(loginEvent);

        LoginResult result = new LoginResult();
        result.setToken(session.getToken());
        result.setRefreshToken(session.getRefreshToken());
        result.setExpiresIn(SESSION_TTL_SECONDS);
        result.setUser(userMapper.toDTO(user));
        result.setForceChangePassword(user.getPasswordChangedAt() == null);
        return result;
    }

    private String exchangeToken(SsoProvider p, String code) {
        try {
            String body = "grant_type=authorization_code"
                    + "&code=" + encode(code)
                    + "&client_id=" + encode(p.getClientId())
                    + "&client_secret=" + encode(com.df4j.xctec.xcms.identity.util.CryptoUtil.decrypt(p.getClientSecret()))
                    + "&redirect_uri=" + encode(p.getRedirectUri());
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(p.getTokenUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(resp.body());
            if (node.has("access_token")) {
                return node.get("access_token").asText();
            }
            if (node.has("id_token")) {
                return node.get("id_token").asText();
            }
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "SSO 换取令牌失败: " + resp.body());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "SSO 换取令牌异常: " + e.getMessage());
        }
    }

    private JsonNode fetchUserInfo(SsoProvider p, String accessToken) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(p.getUserInfoUrl()))
                    .header("Authorization", "Bearer " + accessToken)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readTree(resp.body());
        } catch (Exception e) {
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "SSO 获取用户信息异常: " + e.getMessage());
        }
    }

    private String encode(String v) {
        return v == null ? "" : java.net.URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    @Override
    @Transactional
    public void logout(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            session.setStatus("EXPIRED");
            sessionRepository.save(session);
        });
    }

    @Override
    public TokenInfo validateToken(String token) {
        TokenInfo info = new TokenInfo();
        info.setValid(false);
        UserSession session = sessionRepository.findByToken(token).orElse(null);
        if (session == null || session.getExpireAt().isBefore(LocalDateTime.now())) {
            return info;
        }
        User user = userRepository.findById(session.getUserId()).orElse(null);
        info.setValid(true);
        info.setUserId(session.getUserId());
        info.setTenantId(session.getTenantId());
        info.setExpireAt(session.getExpireAt());
        info.setUsername(user != null ? user.getUsername() : null);
        return info;
    }

    @Override
    @Transactional
    public LoginResult refreshToken(String refreshToken) {
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID));
        if (session.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCodes.AUTH_TOKEN_EXPIRED);
        }
        User user = userRepository.findById(session.getUserId()).orElseThrow(
                () -> new BusinessException(ErrorCodes.USER_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        session.setToken(jwtTokenProvider.generateAccessToken(user.getId(), session.getTenantId(), user.getUsername()));
        session.setRefreshToken(jwtTokenProvider.generateRefreshToken(user.getId(), session.getTenantId(), user.getUsername()));
        session.setLastActiveAt(now);
        sessionRepository.save(session);
        LoginResult result = new LoginResult();
        result.setToken(session.getToken());
        result.setRefreshToken(session.getRefreshToken());
        result.setExpiresIn(SESSION_TTL_SECONDS);
        result.setUser(userMapper.toDTO(user));
        return result;
    }

    @Override
    public SessionDTO getCurrentSession() {
        Long userId = TenantContext.getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return sessionRepository.findByUserId(userId).stream()
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .findFirst()
                .map(this::toDto)
                .orElse(null);
    }

    private String resolveClientIp() {
        try {
            var attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletAttrs) {
                return servletAttrs.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {
            // 非 Web 上下文（如单元测试）忽略
        }
        return null;
    }

    @Override
    public List<SessionDTO> getUserSessions(Long userId) {
        return sessionRepository.findByUserId(userId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public void revokeSession(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            session.setStatus("REVOKED");
            sessionRepository.save(session);
        });
    }

    @Override
    public SessionDTO resolveSession(String token) {
        UserSession session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID));
        if (session.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCodes.AUTH_TOKEN_EXPIRED);
        }
        return toDto(session);
    }

    private SessionDTO toDto(UserSession session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId());
        dto.setTenantId(session.getTenantId());
        dto.setDeviceType(session.getDeviceType());
        dto.setDeviceInfo(session.getDeviceInfo());
        dto.setLoginIp(session.getLoginIp());
        dto.setLoginAt(session.getLoginAt());
        dto.setExpireAt(session.getExpireAt());
        dto.setStatus(session.getStatus());
        return dto;
    }
}
