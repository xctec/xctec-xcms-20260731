package com.df4j.xctec.xcms.app;

import com.df4j.xctec.xcms.identity.api.tenant.TenantResolver;
import com.df4j.xctec.xcms.identity.repository.RoleRepository;
import com.df4j.xctec.xcms.identity.repository.UserRepository;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.org.repository.DepartmentRepository;
import com.df4j.xctec.xcms.tenant.domain.TenantInfo;
import com.df4j.xctec.xcms.tenant.repository.TenantInfoRepository;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 应用上下文加载测试（冒烟）。
 *
 * <p>验证组合根 {@link XcmsApplication} 启动后，所有业务模块的 AutoConfiguration 与 Bean
 * 被正确装配，包括 {@link TenantResolver} SPI 实现与 Hibernate 多租户解析器；并验证平台种子
 * 经事件路径补齐默认租户数据（设计 §5.1）。</p>
 */
@SpringBootTest
@ActiveProfiles("h2")
class XcmsApplicationContextLoadTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TenantInfoRepository tenantInfoRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void contextLoads() {
        assertThat(applicationContext.getBean(TenantResolver.class))
                .as("TenantResolver SPI 实现应被自动装配")
                .isNotNull();
        assertThat(applicationContext.getBean(CurrentTenantIdentifierResolver.class))
                .as("Hibernate 多租户解析器应被自动装配")
                .isNotNull();
    }

    /**
     * 验证 §5.1 修复：默认租户在种子落库后发布 {@code TenantCreatedEvent}，
     * 事件路径补齐 {@code tenant_user} 角色与 ROOT 根部门，且不重复创建 admin。
     */
    @Test
    void seedInitializesDefaultTenantViaEventPath() {
        TenantInfo tenant = tenantInfoRepository.findByTenantCode("default")
                .orElseThrow(() -> new AssertionError("默认租户应已由平台种子创建"));
        Long tenantId = tenant.getId();
        try {
            TenantContext.set(tenantId);
            assertThat(roleRepository.findByTenantIdAndRoleCode(tenantId, "tenant_user"))
                    .as("默认租户应拥有 tenant_user 角色（§5.1 事件路径补齐）")
                    .isPresent();
            assertThat(departmentRepository.findByTenantIdAndParentIdIsNullAndDeletedAtIsNull(tenantId))
                    .as("默认租户应拥有 ROOT 根部门（§5.1 事件路径补齐）")
                    .isNotEmpty();
            assertThat(userRepository.findByTenantIdAndUsername(tenantId, "admin"))
                    .as("默认租户应存在唯一 admin 账号，事件路径不得重复创建")
                    .isPresent();
        } finally {
            TenantContext.clear();
        }
    }
}
