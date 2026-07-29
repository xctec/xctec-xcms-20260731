package com.df4j.xctec.xcms.web;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.auth.api.dto.BusinessVisibilityAuth;
import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import com.df4j.xctec.xcms.auth.api.dto.PermissionDTO;
import com.df4j.xctec.xcms.auth.api.enums.MenuScope;
import com.df4j.xctec.xcms.web.config.XcmsJwtProperties;
import com.df4j.xctec.xcms.web.error.GlobalExceptionHandler;
import com.df4j.xctec.xcms.web.openapi.WebStarterOpenApiAutoConfiguration;
import com.df4j.xctec.xcms.web.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.Filter;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * web-starter 自动装配验证（评审 P3-8：补齐端到端安全行为验证）。
 *
 * <p>覆盖：SecurityFilterChain / GlobalExceptionHandler / OpenAPI 装配，以及
 * 无 token→401、有效 token 无权限→403、permit 路径不被拦截，OpenAPI Bean 唯一。</p>
 */
@SpringBootTest(classes = WebStarterAutoConfigurationTest.TestApplication.class)
@Import({WebStarterAutoConfigurationTest.TestConfig.class, WebStarterAutoConfigurationTest.TestController.class})
class WebStarterAutoConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // 不依赖 spring-boot-test-autoconfigure（@AutoConfigureMockMvc）与 spring-security-test，
        // 手动将 springSecurityFilterChain 加入 MockMvc，确保认证/授权过滤器生效
        Filter springSecurityFilterChain = wac.getBean("springSecurityFilterChain", Filter.class);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(springSecurityFilterChain).build();
    }

    @Test
    void contextLoads() {
        assertTrue(context.getBean(SecurityFilterChain.class) instanceof SecurityFilterChain);
        assertTrue(context.getBean(GlobalExceptionHandler.class) instanceof GlobalExceptionHandler);
    }

    @Test
    void openApiBeanIsUnique() {
        // springdoc 2.x 不注册 OpenAPI Bean，仅 starter 提供一个；@ConditionalOnMissingBean 防御覆盖
        assertEquals(1, context.getBeansOfType(io.swagger.v3.oas.models.OpenAPI.class).size());
    }

    @Test
    void noToken_returns401ApiResponse() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/admin")).andReturn();
        assertEquals(401, result.getResponse().getStatus());
        String contentType = result.getResponse().getContentType();
        assertTrue(contentType != null && contentType.contains("application/json"),
                "401 响应应为 application/json 的 ApiResponse 结构");
        assertTrue(result.getResponse().getContentAsString().contains("error"),
                "401 响应体应包含 ApiResponse.error 结构");
    }

    @Test
    void validTokenWithoutPermission_returns403() throws Exception {
        String token = signedToken();
        MvcResult result = mockMvc.perform(
                get("/api/test/admin").header("Authorization", "Bearer " + token)).andReturn();
        assertEquals(403, result.getResponse().getStatus());
    }

    @Test
    void permitPath_notBlocked() throws Exception {
        MvcResult result = mockMvc.perform(get("/swagger-ui.html")).andReturn();
        // 放行路径不应被重定向到登录（401）
        assertNotEquals(401, result.getResponse().getStatus());
    }

    private String signedToken() throws Exception {
        JWSSigner signer = new MACSigner(XcmsJwtProperties.DEFAULT_SECRET);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("1")
                .claim("tenantId", 1L)
                .claim("username", "tester")
                .claim("token_type", "user")
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(signer);
        return jwt.serialize();
    }

    @SpringBootApplication(scanBasePackages = "com.df4j.xctec.xcms.web")
    static class TestApplication {
    }

    @TestConfiguration
    @Import({SecurityConfig.class, GlobalExceptionHandler.class, WebStarterOpenApiAutoConfiguration.class})
    static class TestConfig {

        @Bean
        PermissionService permissionService() {
            return new PermissionService() {
                @Override
                public boolean checkPermission(Long userId, String permCode) {
                    return false;
                }

                @Override
                public void requirePermission(Long userId, String permCode) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Set<String> getUserPermissions(Long userId) {
                    return Set.of();
                }

                @Override
                public List<MenuDTO> getUserMenus(Long userId, MenuScope scope) {
                    return List.of();
                }

                @Override
                public List<PermissionDTO> listAllPermissions() {
                    return List.of();
                }

                @Override
                public BusinessVisibilityAuth checkBusinessVisibility(Long userId, Long targetTenantId) {
                    return null;
                }
            };
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @RestController
    @RequestMapping("/api/test")
    static class TestController {

        @GetMapping("/admin")
        @PreAuthorize("hasAuthority('xcms:admin')")
        public String admin() {
            return "ok";
        }

        @GetMapping("/ping")
        public String ping() {
            return "pong";
        }
    }
}
