package com.df4j.xctec.xcms.identity.listener;

import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.kernel.event.DomainEventListener;
import com.df4j.xctec.xcms.tenant.api.event.TenantCreatedEvent;
import com.df4j.xctec.xcms.identity.domain.Role;
import com.df4j.xctec.xcms.identity.domain.User;
import com.df4j.xctec.xcms.identity.domain.UserRole;
import com.df4j.xctec.xcms.identity.repository.RoleRepository;
import com.df4j.xctec.xcms.identity.repository.UserRepository;
import com.df4j.xctec.xcms.identity.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * 监听租户创建事件，初始化默认角色与管理员。
 *
 * <p>经统一分发器调度，租户上下文切换由分发器按事件 tenantId 统一完成，
 * 本监听器内不再手动 {@code switchTo}。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantUserInitializer implements DomainEventListener<TenantCreatedEvent> {

    // AT-15：新租户 admin 初始密码随机生成，不再硬编码
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SYMBOLS = "!@#$%^&*_-+=";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SYMBOLS;
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional
    public void onEvent(TenantCreatedEvent event) {
        Long tenantId = event.getTenantId();
        if (tenantId == null) {
            log.warn("TenantCreatedEvent 缺少 tenantId，跳过身份初始化");
            return;
        }
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
            // AT-15：随机初始密码；passwordChangedAt 保持 null，首登强制改密
            String initialPassword = generateRandomPassword();
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode(initialPassword))
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
            // 安全要求（AT-15）：日志不打印明文密码，密码经安全渠道下发
            log.info("租户 {} 默认管理员 admin 初始化完成，初始密码已随机生成（不落日志），请通过安全渠道下发并提示首登改密", tenantId);
        }
    }

    @Override
    public Class<TenantCreatedEvent> eventType() {
        return TenantCreatedEvent.class;
    }

    /**
     * 生成 12 位随机密码（AT-15）：SecureRandom，保证至少各含一个大写、小写、数字、符号；
     * 字符集剔除易混淆字符（I/l/O/0/1）。
     */
    static String generateRandomPassword() {
        char[] pwd = new char[PASSWORD_LENGTH];
        // 前四位保证四类字符各至少一个
        pwd[0] = UPPER.charAt(RANDOM.nextInt(UPPER.length()));
        pwd[1] = LOWER.charAt(RANDOM.nextInt(LOWER.length()));
        pwd[2] = DIGITS.charAt(RANDOM.nextInt(DIGITS.length()));
        pwd[3] = SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length()));
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            pwd[i] = ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length()));
        }
        // Fisher-Yates 洗牌，避免固定模式
        for (int i = PASSWORD_LENGTH - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = pwd[i];
            pwd[i] = pwd[j];
            pwd[j] = tmp;
        }
        return new String(pwd);
    }
}
