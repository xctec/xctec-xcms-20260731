package com.df4j.xctec.xcms.app;

import com.df4j.xctec.xcms.identity.api.tenant.TenantResolver;
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
 * 被正确装配，包括 {@link TenantResolver} SPI 实现与 Hibernate 多租户解析器。</p>
 */
@SpringBootTest
@ActiveProfiles("h2")
class XcmsApplicationContextLoadTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext.getBean(TenantResolver.class))
                .as("TenantResolver SPI 实现应被自动装配")
                .isNotNull();
        assertThat(applicationContext.getBean(CurrentTenantIdentifierResolver.class))
                .as("Hibernate 多租户解析器应被自动装配")
                .isNotNull();
    }
}
