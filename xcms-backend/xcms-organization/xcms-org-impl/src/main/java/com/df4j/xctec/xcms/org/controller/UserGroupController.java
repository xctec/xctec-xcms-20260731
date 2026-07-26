package com.df4j.xctec.xcms.org.controller;

import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.kernel.common.dto.MembersRequest;
import com.df4j.xctec.xcms.org.api.UserGroupService;
import com.df4j.xctec.xcms.org.api.dto.UserGroupCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.UserGroupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户组管理（管理面）。全 POST 风格，URL 为 /admin/user-group/{action}。
 */
@RestController
@RequestMapping("/admin/user-group")
@Tag(name = "用户组 UserGroup", description = "管理面：用户组创建/成员管理")
@RequiredArgsConstructor
public class UserGroupController {

    private final UserGroupService userGroupService;

    @PostMapping("/create")
    public ApiResponse<UserGroupDTO> createGroup(@RequestBody UserGroupCreateRequest request) {
        return ApiResponse.success(userGroupService.createGroup(request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deleteGroup(@RequestBody IdRequest request) {
        userGroupService.deleteGroup(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/add-members")
    public ApiResponse<Void> addMembers(@RequestBody MembersRequest request) {
        userGroupService.addMembers(request.getId(), request.getMemberIds());
        return ApiResponse.success();
    }

    @PostMapping("/remove-members")
    public ApiResponse<Void> removeMembers(@RequestBody MembersRequest request) {
        userGroupService.removeMembers(request.getId(), request.getMemberIds());
        return ApiResponse.success();
    }

    @PostMapping("/list-members")
    public ApiResponse<List<UserBriefDTO>> listGroupMembers(@RequestBody IdRequest request) {
        return ApiResponse.success(userGroupService.listGroupMembers(request.getId()));
    }

    @PostMapping("/list")
    public ApiResponse<List<UserGroupDTO>> listGroups() {
        return ApiResponse.success(userGroupService.listGroups());
    }
}
