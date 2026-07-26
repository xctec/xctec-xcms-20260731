package com.df4j.xctec.xcms.app;

import com.df4j.xctec.xcms.identity.api.tenant.ResolvedTenant;
import com.df4j.xctec.xcms.identity.api.tenant.TenantResolver;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.portal.web.TenantInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * TenantInterceptor 单元测试（对应评审 P2.2 轻量鉴权层）。
 *
 * <p>验证：缺少凭证、token 无效/过期时直接返回 401；有效 token 时解析并写入
 * {@link TenantContext}。拦截器逻辑独立于完整 Spring 上下文，可稳定运行。</p>
 */
@ExtendWith(MockitoExtension.class)
class TenantInterceptorTest {

    @Mock
    private TenantResolver tenantResolver;

    @InjectMocks
    private TenantInterceptor interceptor;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void missingTokenReturns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void validTokenSetsTenantContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(tenantResolver.resolve("valid.jwt.token"))
                .thenReturn(Optional.of(new ResolvedTenant(101L, 7L)));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals(101L, TenantContext.getTenantId());
        assertEquals(7L, TenantContext.getCurrentUserId());
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(tenantResolver.resolve("bad.token")).thenReturn(Optional.empty());

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }
}
