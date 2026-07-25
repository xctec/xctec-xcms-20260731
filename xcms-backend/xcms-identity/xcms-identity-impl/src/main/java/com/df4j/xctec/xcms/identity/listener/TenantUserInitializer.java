package com.df4j.xctec.xcms.identity.listener;

import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.tenant.api.event.TenantCreatedEvent;
import com.df4j.xctec.xcms.identity.domain.Role;
import com.df4j.xctec.xcms.identity.domain.User;
import com.df4j.xctec.xcms.identity.domain.UserRole;
import com.df4j.xctec.xcms.identity.repository.RoleRepository;
import com.df4j.xctec.xcms.identity.repository.UserRepository;
import com.df4j.xctec.xcms.identity.repository.UserRoleRepository;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantUserInitializer {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @EventListener
    @Transactional
    public void onTenantCreated(TenantCreatedEvent event) {
        Long tenantId = event.getTenantId();
        if (tenantId == null) {
            log.warn("TenantCreatedEvent 缺少 tenantId，跳过身份初始化");
            return;
        }
        TenantContext.TenantInfo original = TenantContext.switchTo(tenantId);
        try {
            Role adminRole = roleRepository.findByRoleCode("tenant_admin").orElseGet(() ->
                    roleRepository.save(Role.builder()
                            .roleCode("tenant_admin")
                            .roleName("租户管理员")
                            .roleType(RoleScope.TENANT.name())
                            .description("租户默认管理员角色")
                            .build()));

            roleRepository.findByRoleCode("tenant_user").orElseGet(() ->
                    roleRepository.save(Role.builder()
                            .roleCode("tenant_user")
                            .roleName("普通用户")
                            .roleType(RoleScope.TENANT.name())
                            .description("租户默认用户角色")
                            .build()));

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .realName("系统管理员")
                        .status(UserStatus.ACTIVE)
                        .build();
                admin = userRepository.save(admin);
                if (userRoleRepository.findByUserIdAndRoleId(admin.getId(), adminRole.getId()).isEmpty()) {
                    userRoleRepository.save(UserRole.builder()
                            .userId(admin.getId())
                            .roleId(adminRole.getId())
                            .build());
                }
                log.info("租户 {} 默认管理员 admin 初始化完成（初始密码: admin123，请尽快修改）", tenantId);
            }
        } finally {
            TenantContext.restore(original);
        }
    }
}
