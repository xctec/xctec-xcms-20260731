package com.df4j.xctec.xcms.identity.service;

import com.df4j.xctec.xcms.identity.api.RoleService;
import com.df4j.xctec.xcms.identity.api.UserService;
import com.df4j.xctec.xcms.identity.api.dto.BatchImportResult;
import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.identity.api.dto.UserCreateRequest;
import com.df4j.xctec.xcms.identity.api.dto.UserDTO;
import com.df4j.xctec.xcms.identity.api.dto.UserQuery;
import com.df4j.xctec.xcms.identity.api.dto.UserUpdateRequest;
import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.identity.api.event.UserCreatedEvent;
import com.df4j.xctec.xcms.identity.domain.User;
import com.df4j.xctec.xcms.identity.mapper.UserMapper;
import com.df4j.xctec.xcms.identity.repository.UserRepository;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher eventPublisher;
    private final RoleService roleService;

    @Override
    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        requireTenant();
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCodes.USER_ALREADY_EXISTS, "用户名已存在: " + request.getUsername());
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .build();
        User saved = userRepository.save(user);

        if (request.getRoleIds() != null) {
            Long tenantId = TenantContext.getTenantId();
            for (Long roleId : request.getRoleIds()) {
                roleService.assignRoleToUser(saved.getId(), roleId, RoleScope.TENANT, String.valueOf(tenantId));
            }
        }

        UserCreatedEvent event = new UserCreatedEvent();
        event.setUserId(saved.getId());
        event.setTenantId(TenantContext.getTenantId());
        event.setUsername(saved.getUsername());
        event.setRealName(saved.getRealName());
        event.setDeptId(request.getDeptId());
        event.setPositionId(request.getPositionId());
        eventPublisher.publish(event);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long userId, UserUpdateRequest request) {
        requireTenant();
        User user = getById(userId);
        if (StringUtils.hasText(request.getRealName())) user.setRealName(request.getRealName());
        if (StringUtils.hasText(request.getEmail())) user.setEmail(request.getEmail());
        if (StringUtils.hasText(request.getPhone())) user.setPhone(request.getPhone());
        if (StringUtils.hasText(request.getAvatar())) user.setAvatar(request.getAvatar());
        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO getUserById(Long userId) {
        requireTenant();
        return toDTO(getById(userId));
    }

    @Override
    public UserDTO getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND, "用户不存在: " + username));
        return toDTO(user);
    }

    @Override
    public UserDTO getCurrentUser() {
        Long uid = TenantContext.getCurrentUserId();
        if (uid == null) {
            throw new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID, "未登录或会话已失效");
        }
        return getUserById(uid);
    }

    @Override
    public PageResult<UserDTO> listUsers(UserQuery query) {
        requireTenant();
        org.springframework.data.domain.Pageable pageable = PageRequest.of(
                Math.max(query.getPage() - 1, 0), Math.max(query.getSize(), 1),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<User> page = userRepository.search(query.getKeyword(), query.getStatus(), pageable);
        return PageResult.of(userMapper.toDTOList(page.getContent()), page.getTotalElements());
    }

    @Override
    @Transactional
    public void changeUserStatus(Long userId, UserStatus status) {
        requireTenant();
        User user = getById(userId);
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        requireTenant();
        User user = getById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        requireTenant();
        User user = getById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public BatchImportResult batchImportUsers(List<UserCreateRequest> users) {
        requireTenant();
        int success = 0;
        int failed = 0;
        for (UserCreateRequest req : users) {
            try {
                createUser(req);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }
        BatchImportResult result = new BatchImportResult();
        result.setTotal(users.size());
        result.setSuccess(success);
        result.setFailed(failed);
        return result;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        requireTenant();
        User user = getById(userId);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public List<UserBriefDTO> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return userRepository.findAllById(ids).stream()
                .filter(u -> u.getDeletedAt() == null)
                .map(u -> {
                    UserBriefDTO d = new UserBriefDTO();
                    d.setId(u.getId());
                    d.setUsername(u.getUsername());
                    d.setRealName(u.getRealName());
                    return d;
                }).toList();
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = userMapper.toDTO(user);
        List<RoleDTO> roles = roleService.getUserRoles(user.getId());
        dto.setRoles(roles);
        return dto;
    }

    private User getById(Long userId) {
        return userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND, "用户不存在: " + userId));
    }

    private void requireTenant() {
        if (TenantContext.getTenantId() == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "缺少租户上下文");
        }
    }
}
