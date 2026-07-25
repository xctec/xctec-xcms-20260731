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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门管理（管理面）。全 POST 风格，URL 为 /admin/department/{action}。
 */
@RestController
@RequestMapping("/admin/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final OrganizationService organizationService;

    @PostMapping("/create")
    public ApiResponse<DepartmentDTO> createDepartment(@RequestBody DepartmentCreateRequest request) {
        return ApiResponse.success(organizationService.createDepartment(request));
    }

    @PostMapping("/update")
    public ApiResponse<DepartmentDTO> updateDepartment(@RequestBody DepartmentUpdateRequest request) {
        return ApiResponse.success(organizationService.updateDepartment(request.getId(), request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deleteDepartment(@RequestBody IdRequest request) {
        organizationService.deleteDepartment(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/get")
    public ApiResponse<DepartmentDTO> getDepartment(@RequestBody IdRequest request) {
        return ApiResponse.success(organizationService.getDepartment(request.getId()));
    }

    @PostMapping("/tree")
    public ApiResponse<List<DepartmentTreeDTO>> getDepartmentTree() {
        return ApiResponse.success(organizationService.getDepartmentTree());
    }

    @PostMapping("/list-children")
    public ApiResponse<List<DepartmentDTO>> listSubDepartments(@RequestBody IdRequest request) {
        return ApiResponse.success(organizationService.listSubDepartments(request.getId()));
    }

    @PostMapping("/move")
    public ApiResponse<Void> moveDepartment(@RequestBody MoveRequest request) {
        organizationService.moveDepartment(request.getId(), request.getTargetId());
        return ApiResponse.success();
    }

    @PostMapping("/list-users")
    public ApiResponse<PageResult<UserBriefDTO>> listDepartmentUsers(@RequestBody DepartmentUsersRequest request) {
        return ApiResponse.success(organizationService.listDepartmentUsers(request.getDeptId(), request.getPage()));
    }
}
