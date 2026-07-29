package com.df4j.xctec.xcms.web;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.auth.api.dto.BusinessVisibilityAuth;
import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import com.df4j.xctec.xcms.auth.api.dto.PermissionDTO;
import com.df4j.xctec.xcms.auth.api.enums.MenuScope;
import com.df4j.xctec.xcms.web.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.df4j.xctec.xcms.web.openapi.WebStarterOpenApiAutoConfiguration;
import com.df4j.xctec.xcms.web.security.SecurityConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * web-starter 自动配置装配校验（AT-29）。
 *
 * <p>验证三类核心 Bean 均被正确装配：{@link SecurityConfig}（SecurityFilterChain）、
 * {@link GlobalExceptionHandler}（全局异常）、{@link WebStarterOpenApiAutoConfiguration}
 * 提供的 OpenAPI Bean（且与 springdoc 默认 Bean 不冲突，全局唯一）。</p>
 *
 * <p>PermissionService 由内置桩 Bean 提供，仅用于满足 SecurityFilterChain 的方法参数，
 * 不依赖任何业务模块或 Mockito。</p>
 */
@SpringBootTest
class WebStarterAutoConfigurationTest {

    @SpringBootApplication(scanBasePackages = "com.df4j.xctec.xcms.web")
    static class TestApplication {
        @Bean
        PermissionService permissionService() {
            return new PermissionService() {
                @Override
                public boolean checkPermission(Long userId, String permCode) {
                    return false;
                }

                @Override
                public void requirePermission(Long userId, String permCode) {
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

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsAndWebStarterBeansRegistered() {
        assertNotNull(context.getBean(SecurityConfig.class));
        assertNotNull(context.getBean(SecurityFilterChain.class));
        assertNotNull(context.getBean(GlobalExceptionHandler.class));
        assertNotNull(context.getBean(WebStarterOpenApiAutoConfiguration.class));
        // 关键：OpenAPI Bean 全局唯一，避免与 springdoc 默认 Bean 冲突
        assertEquals(1, context.getBeansOfType(OpenAPI.class).size());
    }
}
