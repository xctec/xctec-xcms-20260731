package com.df4j.xctec.xcms.identity.api;

import com.df4j.xctec.xcms.identity.api.dto.LoginRequest;
import com.df4j.xctec.xcms.identity.api.dto.LoginResult;
import com.df4j.xctec.xcms.identity.api.dto.SessionDTO;
import com.df4j.xctec.xcms.identity.api.dto.TokenInfo;

import java.util.List;

/**
 * 认证服务
 */
public interface AuthService {

    LoginResult login(LoginRequest request);

    LoginResult ssoCallback(String code, String state);

    void logout(String token);

    TokenInfo validateToken(String token);

    LoginResult refreshToken(String refreshToken);

    SessionDTO getCurrentSession();

    List<SessionDTO> getUserSessions(Long userId);

    void revokeSession(String token);

    /**
     * 根据 token 解析会话（供门户层设置租户/用户上下文）
     */
    SessionDTO resolveSession(String token);
}
