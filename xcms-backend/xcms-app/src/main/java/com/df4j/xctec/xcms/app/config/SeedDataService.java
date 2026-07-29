package com.df4j.xctec.xcms.app.config;

import com.df4j.xctec.xcms.auth.domain.Permission;
import com.df4j.xctec.xcms.auth.domain.RolePermission;
import com.df4j.xctec.xcms.auth.repository.PermissionRepository;
import com.df4j.xctec.xcms.auth.repository.RolePermissionRepository;
import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.identity.domain.Role;
import com.df4j.xctec.xcms.identity.domain.User;
import com.df4j.xctec.xcms.identity.domain.UserRole;
import com.df4j.xctec.xcms.identity.listener.TenantUserInitializer;
import com.df4j.xctec.xcms.identity.repository.RoleRepository;
import com.df4j.xctec.xcms.identity.repository.UserRepository;
import com.df4j.xctec.xcms.identity.repository.UserRoleRepository;
import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import com.df4j.xctec.xcms.tenant.api.enums.TenantType;
import com.df4j.xctec.xcms.tenant.domain.TenantInfo;
import com.df4j.xctec.xcms.tenant.repository.TenantInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    private static final String ADMIN_ROLE_CODE = "tenant_admin";

    /**
     * 默认管理员初始密码（设计 §5.2 / AT-15）：留空则随机生成并首登强制改密；
     * 生产环境应通过环境变量/密钥注入 {@code xcms.seed.admin-password}，避免启动日志暴露明文。
     */
    @Value("${xcms.seed.admin-password:}")
    private String configuredAdminPassword;

    private final TenantInfoRepository tenantInfoRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    /** AT-08：操作权限（perm_operation）仓库，平台级，无租户隔离 */
    private final PermissionRepository permissionRepository;
    /** AT-08：角色-权限绑定（perm_role_permission）仓库，租户级 */
    private final RolePermissionRepository rolePermissionRepository;
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
            // 设计 §5.2 / AT-15：初始密码随机生成，与每租户事件路径（TenantUserInitializer）保持同一套逻辑；
            // 若运维已通过 xcms.seed.admin-password 显式注入（已知晓），则复用该密码且不强制改密。
            boolean useConfigured = configuredAdminPassword != null && !configuredAdminPassword.isBlank();
            String rawPassword = useConfigured ? configuredAdminPassword : TenantUserInitializer.generateRandomPassword();
            admin = User.builder()
                    .username(ADMIN_USERNAME)
                    .password(passwordEncoder.encode(rawPassword))
                    .realName("系统管理员")
                    .userType("ADMIN")
                    .status(UserStatus.ACTIVE)
                    .build();
            // 随机初始密码 → passwordChangedAt 置 null，登录返回 forceChangePassword=true，首登强制改密；
            // 配置注入密码 → 置当前时间，不强制改密。
            admin.setPasswordChangedAt(useConfigured ? LocalDateTime.now() : null);
            admin = userRepository.save(admin);
            if (useConfigured) {
                log.info("[Seed] 已创建管理员账号(密码由配置注入，不强制改密): tenantId={}, username={}",
                        tenantId, ADMIN_USERNAME);
            } else {
                // 默认平台租户无安全下发渠道，启动期一次性明文打印以便首次登录；
                // 生产环境请注入 xcms.seed.admin-password 以避免日志暴露明文。
                log.warn("[Seed] 默认管理员初始密码(随机生成，请立即记录并于首登后修改): username={}, password={}",
                        ADMIN_USERNAME, rawPassword);
            }
        }

        Role role = roleRepository.findByRoleCode(ADMIN_ROLE_CODE).orElse(null);
        if (role == null) {
            role = Role.builder()
                    .roleCode(ADMIN_ROLE_CODE)
                    .roleName("租户管理员")
                    // 设计 §5.3：统一为枚举值 RoleScope.TENANT（与事件路径、createDefaultRolesForTenant 保持一致）
                    .roleType(RoleScope.TENANT.name())
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

        log.info("[Seed] 默认登录账号就绪: tenantId={}, username={}", tenantId, ADMIN_USERNAME);

        // AT-08：补齐声明式鉴权所需的操作权限，并授予管理员角色，避免 @PreAuthorize 恒 403
        seedOperationPermissions(role);
    }

    /** AT-08：声明式鉴权（@PreAuthorize）所需的操作权限全集，与前端权限码对齐。 */
    private static final List<String[]> OPERATION_PERMISSIONS = List.of(
            new String[]{"tenant:create", "新建租户", "租户管理", "TENANT", "create"},
            new String[]{"tenant:edit", "编辑租户", "租户管理", "TENANT", "update"},
            new String[]{"tenant:delete", "删除租户", "租户管理", "TENANT", "delete"},
            new String[]{"org:dept:create", "新建部门", "组织架构", "DEPARTMENT", "create"},
            new String[]{"org:position:create", "新建岗位", "组织架构", "POSITION", "create"},
            new String[]{"org:group:create", "新建用户组", "组织架构", "USER_GROUP", "create"},
            new String[]{"org:edit", "编辑组织", "组织架构", "ORG", "update"},
            new String[]{"user:create", "新建用户", "用户管理", "USER", "create"},
            new String[]{"user:edit", "编辑用户", "用户管理", "USER", "update"},
            new String[]{"user:delete", "删除用户", "用户管理", "USER", "delete"},
            new String[]{"user:export", "导出用户", "用户管理", "USER", "read"},
            new String[]{"user:reset-pwd", "重置密码", "用户管理", "USER", "update"},
            new String[]{"role:create", "新建角色", "权限管理", "ROLE", "create"},
            new String[]{"role:edit", "编辑角色", "权限管理", "ROLE", "update"},
            new String[]{"role:delete", "删除角色", "权限管理", "ROLE", "delete"},
            new String[]{"role:assign", "分配角色", "权限管理", "ROLE", "update"},
            new String[]{"role:permission", "分配权限", "权限管理", "ROLE", "update"},
            new String[]{"role:data-scope", "数据范围", "权限管理", "ROLE", "update"},
            new String[]{"workflow:deploy", "流程部署", "流程管理", "WORKFLOW", "update"}
    );

    /**
     * AT-08：幂等补齐操作权限种子，并授予默认租户管理员角色。
     * 声明式鉴权依赖 JWT 解析出的权限码，而权限码来源于 {@code perm_operation.perm_code}（经角色-权限绑定）。
     * 若不补齐，{@code @PreAuthorize} 会对所有人恒返回 403，管理员也将无法执行任何写操作。
     * 本方法在租户上下文内执行：perm_operation 为平台级（无 @TenantId），角色-权限绑定为租户级。
     */
    private void seedOperationPermissions(Role adminRole) {
        for (String[] def : OPERATION_PERMISSIONS) {
            String code = def[0], name = def[1], module = def[2], resourceType = def[3], action = def[4];
            Permission perm = permissionRepository.findByPermCode(code)
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .permCode(code).permName(name).module(module)
                            .resourceType(resourceType).action(action).build()));
            if (rolePermissionRepository.findByRoleIdAndPermissionId(adminRole.getId(), perm.getId()).isEmpty()) {
                rolePermissionRepository.save(RolePermission.builder()
                        .roleId(adminRole.getId()).permissionId(perm.getId())
                        .permType("OPERATION").build());
            }
        }
        log.info("[Seed] AT-08 操作权限已就绪: role={}, count={}", ADMIN_ROLE_CODE, OPERATION_PERMISSIONS.size());
    }
}
