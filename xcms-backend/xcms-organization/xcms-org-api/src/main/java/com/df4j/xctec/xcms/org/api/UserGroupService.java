package com.df4j.xctec.xcms.org.api;

import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.org.api.dto.UserGroupCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.UserGroupDTO;

import java.util.List;

/**
 * 用户组服务
 */
public interface UserGroupService {

    UserGroupDTO createGroup(UserGroupCreateRequest request);

    void deleteGroup(Long groupId);

    void addMembers(Long groupId, List<Long> userIds);

    void removeMembers(Long groupId, List<Long> userIds);

    List<UserBriefDTO> listGroupMembers(Long groupId);

    List<UserGroupDTO> listGroups();
}
