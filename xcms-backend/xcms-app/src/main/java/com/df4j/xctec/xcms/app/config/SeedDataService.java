package com.df4j.xctec.xcms.app.config;

import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.identity.domain.Role;
import com.df4j.xctec.xcms.identity.domain.User;
import com.df4j.xctec.xcms.identity.domain.UserRole;
import com.df4j.xctec.xcms.identity.repository.RoleRepository;
import com.df4j.xctec.xcms.identity.repository.UserRepository;
import com.df4j.xctec.xcms.identity.repository.UserRoleRepository;
import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import com.df4j.xctec.xcms.tenant.domain.TenantInfo;
import com.df4j.xctec.xcms.tenant.repository.TenantInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 种子数据服务。
 *
 * <p>拆分为两段<b>独立事务</b>，是规避 Hibernate {@code @TenantId} "assigned tenant id differs
 * from current tenant id" 报错的关键：{@code @TenantId} 的当前租户由 Hibernate 会话在<b>开启时</b>
 * 通过 {@code CurrentTenantIdentifierResolver} 捕获，会话开启后中途修改 {@link
 * com.df4j.xctec.xcms.kernel.context.TenantContext} 无法改变会话租户。
 *
 * <ol>
 *   <li>{@link #ensureDefaultTenant()}：建默认租户（{@link TenantInfo} 系统级、无 {@code @TenantId}），
 *       可在无租户上下文的事务中执行，返回租户ID。</li>
 *   <li>{@link #seedAdmin(Long)}：由调用方先 {@code TenantContext.set(tenantId)} 后再调用，
 *       使本方法事务开启时会话即捕获到正确租户，随后创建管理员/角色/授权。</li>
 * </ol>
 *
 * <p>全程幂等，重复启动不会重复插入或覆盖已有数据。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedDataService {

    private static final String DEFAULT_TENANT_CODE = "default";
    private static final String DEFAULT_TENANT_NAME = "默认租户";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_ROLE_CODE = "tenant_admin";

    private final TenantInfoRepository tenantInfoRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 确保默认租户存在（系统级表，无 @TenantId 隔离，无需租户上下文）。
     *
     * @return 默认租户 ID
     */
    @Transactional
    public Long ensureDefaultTenant() {
        TenantInfo tenant = tenantInfoRepository.findByTenantCode(DEFAULT_TENANT_CODE).orElse(null);
        if (tenant == null) {
            tenant = new TenantInfo();
            tenant.setTenantCode(DEFAULT_TENANT_CODE);
            tenant.setTenantName(DEFAULT_TENANT_NAME);
            tenant.setTenantType(TenantType.PLATFORM);
            tenant.setLevel(0);
            tenant.setPath("/");
            tenant.setStatus(TenantStatus.ACTIVE);
            tenant = tenantInfoRepository.save(tenant);
            tenant.setPath("/" + tenant.getId() + "/");
            tenant = tenantInfoRepository.save(tenant);
            log.info("[Seed] 已创建默认租户: id={}, code={}", tenant.getId(), DEFAULT_TENANT_CODE);
        }
        return tenant.getId();
    }

    /**
     * 种子管理员账号/角色/授权（租户级表，带 @TenantId）。
     *
     * <p><b>调用约定</b>：调用方必须先 {@code TenantContext.set(tenantId)}，使本方法事务开启时
     * Hibernate 会话捕获到 {@code tenantId}，由 {@code @TenantId} 自动写入 tenant_id 列。
     *
     * @param tenantId 默认租户 ID
     */
    @Transactional
    public void seedAdmin(Long tenantId) {
        User admin = userRepository.findByUsername(ADMIN_USERNAME).orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .username(ADMIN_USERNAME)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .realName("系统管理员")
                    .userType("ADMIN")
                    .status(UserStatus.ACTIVE)
                    .build();
            admin.setPasswordChangedAt(LocalDateTime.now());
            admin = userRepository.save(admin);
            log.info("[Seed] 已创建管理员账号: tenantId={}, username={}", tenantId, ADMIN_USERNAME);
        }

        Role role = roleRepository.findByRoleCode(ADMIN_ROLE_CODE).orElse(null);
        if (role == null) {
            role = Role.builder()
                    .roleCode(ADMIN_ROLE_CODE)
                    .roleName("租户管理员")
                    .roleType("SYSTEM")
                    .roleScope(RoleScope.TENANT)
                    .status("ACTIVE")
                    .build();
            role = roleRepository.save(role);
            log.info("[Seed] 已创建角色: {}", ADMIN_ROLE_CODE);
        }

        if (userRoleRepository.findByUserIdAndRoleId(admin.getId(), role.getId()).isEmpty()) {
            UserRole userRole = UserRole.builder()
                    .userId(admin.getId())
                    .roleId(role.getId())
                    .scopeType("TENANT")
                    .scopeValue(String.valueOf(tenantId))
                    .grantedAt(LocalDateTime.now())
                    .build();
            userRoleRepository.save(userRole);
            log.info("[Seed] 已为管理员分配角色: {}", ADMIN_ROLE_CODE);
        }

        log.info("[Seed] 默认登录凭据 => tenantId={}, username={}, password={}",
                tenantId, ADMIN_USERNAME, ADMIN_PASSWORD);
    }
}
