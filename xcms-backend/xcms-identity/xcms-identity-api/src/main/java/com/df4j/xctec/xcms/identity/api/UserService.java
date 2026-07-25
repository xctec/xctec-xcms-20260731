package com.df4j.xctec.xcms.identity.api;

import com.df4j.xctec.xcms.identity.api.dto.BatchImportResult;
import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.identity.api.dto.UserCreateRequest;
import com.df4j.xctec.xcms.identity.api.dto.UserDTO;
import com.df4j.xctec.xcms.identity.api.dto.UserQuery;
import com.df4j.xctec.xcms.identity.api.dto.UserUpdateRequest;
import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.kernel.common.PageResult;

import java.util.List;

/**
 * 用户服务
 */
public interface UserService {

    UserDTO createUser(UserCreateRequest request);

    UserDTO updateUser(Long userId, UserUpdateRequest request);

    UserDTO getUserById(Long userId);

    UserDTO getByUsername(String username);

    UserDTO getCurrentUser();

    PageResult<UserDTO> listUsers(UserQuery query);

    void changeUserStatus(Long userId, UserStatus status);

    void resetPassword(Long userId, String newPassword);

    void changePassword(Long userId, String oldPassword, String newPassword);

    BatchImportResult batchImportUsers(List<UserCreateRequest> users);

    void deleteUser(Long userId);

    /**
     * 批量获取用户简要信息（跨模块使用）
     */
    List<UserBriefDTO> getUsersByIds(List<Long> ids);
}
