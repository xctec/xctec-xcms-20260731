package com.df4j.xctec.xcms.app;

import com.df4j.xctec.xcms.kernel.context.ActorContext;
import com.df4j.xctec.xcms.portal.web.TenantInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TenantInterceptor 单元测试（AT-07，ADR-016）。
 *
 * <p>认证已上收 Spring Security，拦截器退化为纯上下文填充器：
 * 有认证信息时填充 {@link TenantContext}；无认证信息时放行且不写 401
 * （401 由 Security 层负责）。</p>
 */
class TenantInterceptorTest {

    private final TenantInterceptor interceptor = new TenantInterceptor();

    @AfterEach
    void tearDown() {
        ActorContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void anonymousRequestPassesThroughWithout401() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals(200, response.getStatus());
        assertNull(ActorContext.getTenantId());
    }

    @Test
    void authenticatedJwtFillsTenantContext() {
        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "HS256"),
                Map.of("sub", "7", "tenantId", 101L, "username", "alice"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals(101L, ActorContext.getTenantId());
        assertEquals(7L, ActorContext.getCurrentUserId());
    }

    @Test
    void afterCompletionClearsContext() {
        ActorContext.setUser(101L, 7L);

        interceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);

        assertNull(ActorContext.getTenantId());
    }
}
