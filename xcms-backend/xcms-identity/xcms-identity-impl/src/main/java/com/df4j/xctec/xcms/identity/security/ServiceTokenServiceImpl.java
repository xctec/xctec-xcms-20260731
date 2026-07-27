package com.df4j.xctec.xcms.identity.security;

import com.df4j.xctec.xcms.identity.api.ServiceTokenService;
import org.springframework.stereotype.Service;

/**
 * 服务令牌签发实现（AT-11）：复用 {@link JwtTokenProvider} 的签名密钥，
 * 保证 Resource Server 侧可用同一密钥校验。
 */
@Service
public class ServiceTokenServiceImpl implements ServiceTokenService {

    private final JwtTokenProvider jwtTokenProvider;

    public ServiceTokenServiceImpl(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String issueServiceToken(Long tenantId, String serviceName, long ttlSeconds) {
        return jwtTokenProvider.issueServiceToken(tenantId, serviceName, ttlSeconds);
    }
}
