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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    @Override
    @Transactional
    public LoginResult login(LoginRequest request) {
        Long tenantId = request.getTenantId() != null ? request.getTenantId() : TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "无法确定租户");
        }
        TenantContext.set(tenantId);
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
        return result;
    }

    @Override
    public LoginResult ssoCallback(String code, String state) {
        throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "SSO 暂未启用");
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
