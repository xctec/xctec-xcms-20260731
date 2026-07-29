package com.df4j.xctec.xcms.org.controller;

import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.kernel.common.dto.MembersRequest;
import com.df4j.xctec.xcms.kernel.common.dto.MoveRequest;
import com.df4j.xctec.xcms.org.api.OrganizationService;
import com.df4j.xctec.xcms.org.api.dto.DepartmentCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.DepartmentDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentTreeDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentUpdateRequest;
import com.df4j.xctec.xcms.org.api.dto.DepartmentUsersRequest;
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
 * 部门管理（管理面）。全 POST 风格，URL 为 /admin/department/{action}。
 */
@RestController
@RequestMapping("/api/admin/department")
@Tag(name = "部门 Department", description = "管理面：部门创建/层级/成员管理")
@RequiredArgsConstructor
public class DepartmentController {

    private final OrganizationService organizationService;

    @Operation(summary = "创建部门")
    @PreAuthorize("hasAuthority('org:dept:create') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/create")
    public ApiResponse<DepartmentDTO> createDepartment(@RequestBody DepartmentCreateRequest request) {
        return ApiResponse.success(organizationService.createDepartment(request));
    }

    @Operation(summary = "更新部门")
    @PreAuthorize("hasAuthority('org:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/update")
    public ApiResponse<DepartmentDTO> updateDepartment(@RequestBody DepartmentUpdateRequest request) {
        return ApiResponse.success(organizationService.updateDepartment(request.getId(), request));
    }

    @Operation(summary = "删除部门")
    @PreAuthorize("hasAuthority('org:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/delete")
    public ApiResponse<Void> deleteDepartment(@RequestBody IdRequest request) {
        organizationService.deleteDepartment(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询部门详情")
    @PostMapping("/get")
    public ApiResponse<DepartmentDTO> getDepartment(@RequestBody IdRequest request) {
        return ApiResponse.success(organizationService.getDepartment(request.getId()));
    }

    @Operation(summary = "查询部门树", description = "返回当前租户的部门层级树")
    @PostMapping("/tree")
    public ApiResponse<List<DepartmentTreeDTO>> getDepartmentTree() {
        return ApiResponse.success(organizationService.getDepartmentTree());
    }

    @Operation(summary = "查询子部门列表")
    @PostMapping("/list-children")
    public ApiResponse<List<DepartmentDTO>> listSubDepartments(@RequestBody IdRequest request) {
        return ApiResponse.success(organizationService.listSubDepartments(request.getId()));
    }

    @Operation(summary = "移动部门", description = "调整部门归属（父子关系）")
    @PreAuthorize("hasAuthority('org:edit') or hasAuthority('ROLE_SERVICE')")
    @PostMapping("/move")
    public ApiResponse<Void> moveDepartment(@RequestBody MoveRequest request) {
        organizationService.moveDepartment(request.getId(), request.getTargetId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询部门成员", description = "分页查询部门下的用户")
    @PostMapping("/list-users")
    public ApiResponse<PageResult<UserBriefDTO>> listDepartmentUsers(@RequestBody DepartmentUsersRequest request) {
        return ApiResponse.success(organizationService.listDepartmentUsers(request.getDeptId(), request.getPage()));
    }
}
