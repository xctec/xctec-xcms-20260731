package com.df4j.xctec.xcms.org.controller;

import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.kernel.common.dto.MembersRequest;
import com.df4j.xctec.xcms.org.api.UserGroupService;
import com.df4j.xctec.xcms.org.api.dto.UserGroupCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.UserGroupDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @Operation(summary = "创建用户组")
    @PreAuthorize("hasAuthority('org:group:create') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/create")
    public ApiResponse<UserGroupDTO> createGroup(@RequestBody UserGroupCreateRequest request) {
        return ApiResponse.success(userGroupService.createGroup(request));
    }

    @Operation(summary = "删除用户组")
    @PreAuthorize("hasAuthority('org:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/delete")
    public ApiResponse<Void> deleteGroup(@RequestBody IdRequest request) {
        userGroupService.deleteGroup(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "添加组成员")
    @PreAuthorize("hasAuthority('org:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/add-members")
    public ApiResponse<Void> addMembers(@RequestBody MembersRequest request) {
        userGroupService.addMembers(request.getId(), request.getMemberIds());
        return ApiResponse.success();
    }

    @Operation(summary = "移除组成员")
    @PreAuthorize("hasAuthority('org:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/remove-members")
    public ApiResponse<Void> removeMembers(@RequestBody MembersRequest request) {
        userGroupService.removeMembers(request.getId(), request.getMemberIds());
        return ApiResponse.success();
    }

    @Operation(summary = "查询组成员列表")
    @PostMapping("/list-members")
    public ApiResponse<List<UserBriefDTO>> listGroupMembers(@RequestBody IdRequest request) {
        return ApiResponse.success(userGroupService.listGroupMembers(request.getId()));
    }

    @Operation(summary = "查询用户组列表")
    @PostMapping("/list")
    public ApiResponse<List<UserGroupDTO>> listGroups() {
        return ApiResponse.success(userGroupService.listGroups());
    }
}
