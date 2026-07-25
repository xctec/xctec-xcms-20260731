package com.df4j.xctec.xcms.org.service;

import com.df4j.xctec.xcms.identity.api.UserService;
import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.org.api.UserGroupService;
import com.df4j.xctec.xcms.org.api.dto.UserGroupCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.UserGroupDTO;
import com.df4j.xctec.xcms.org.domain.UserGroup;
import com.df4j.xctec.xcms.org.domain.UserGroupMember;
import com.df4j.xctec.xcms.org.mapper.UserGroupMapper;
import com.df4j.xctec.xcms.org.repository.UserGroupMemberRepository;
import com.df4j.xctec.xcms.org.repository.UserGroupRepository;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupRepository groupRepository;
    private final UserGroupMemberRepository memberRepository;
    private final UserGroupMapper groupMapper;
    private final UserService userService;

    @Override
    @Transactional
    public UserGroupDTO createGroup(UserGroupCreateRequest request) {
        requireTenant();
        if (groupRepository.existsByGroupNameAndDeletedAtIsNull(request.getGroupName())) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, request.getGroupName());
        }
        UserGroup group = new UserGroup();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setType(request.getType());
        group.setCreatedBy(TenantContext.getCurrentUserId());
        group = groupRepository.save(group);
        if (request.getMemberIds() != null) {
            addMembers(group.getId(), request.getMemberIds());
        }
        return toDtoWithCount(group);
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId) {
        requireTenant();
        UserGroup group = findGroup(groupId);
        group.setDeletedAt(LocalDateTime.now());
        group.setUpdatedBy(TenantContext.getCurrentUserId());
        groupRepository.save(group);
        // 成员一并软删除，与用户组删除策略保持一致（评审 P1.3）
        memberRepository.findByGroupId(groupId).forEach(m -> m.setDeletedAt(LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void addMembers(Long groupId, List<Long> userIds) {
        requireTenant();
        findGroup(groupId);
        for (Long userId : userIds) {
            if (memberRepository.findByGroupIdAndUserId(groupId, userId).isEmpty()) {
                UserGroupMember member = UserGroupMember.builder()
                        .groupId(groupId).userId(userId)
                        .createdBy(TenantContext.getCurrentUserId()).build();
                memberRepository.save(member);
            }
        }
    }

    @Override
    @Transactional
    public void removeMembers(Long groupId, List<Long> userIds) {
        requireTenant();
        findGroup(groupId);
        for (Long userId : userIds) {
            // 软删除，与部门/岗位的删除策略保持一致（评审 P1.3）
            memberRepository.findByGroupIdAndUserId(groupId, userId).ifPresent(m -> {
                m.setDeletedAt(LocalDateTime.now());
                memberRepository.save(m);
            });
        }
    }

    @Override
    public List<UserBriefDTO> listGroupMembers(Long groupId) {
        requireTenant();
        findGroup(groupId);
        List<UserBriefDTO> list = new ArrayList<>();
        for (var m : memberRepository.findByGroupIdAndDeletedAtIsNull(groupId)) {
            UserBriefDTO dto = new UserBriefDTO();
            dto.setId(m.getUserId());
            try {
                var user = userService.getUserById(m.getUserId());
                dto.setUsername(user.getUsername());
                dto.setRealName(user.getRealName());
            } catch (Exception ignored) {
            }
            list.add(dto);
        }
        return list;
    }

    @Override
    public List<UserGroupDTO> listGroups() {
        requireTenant();
        return groupRepository.findByDeletedAtIsNull().stream().map(this::toDtoWithCount).toList();
    }

    private UserGroupDTO toDtoWithCount(UserGroup group) {
        UserGroupDTO dto = groupMapper.toDTO(group);
        dto.setMemberCount((int) memberRepository.countByGroupId(group.getId()));
        return dto;
    }

    private UserGroup findGroup(Long groupId) {
        return groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.GROUP_NOT_FOUND, String.valueOf(groupId)));
    }

    private void requireTenant() {
        if (TenantContext.getTenantId() == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "未确定租户上下文");
        }
    }
}
