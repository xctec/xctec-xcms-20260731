package com.df4j.xctec.xcms.org.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.org.api.UserOrgService;
import com.df4j.xctec.xcms.org.api.dto.DepartmentDTO;
import com.df4j.xctec.xcms.org.api.dto.UserOrgAssignRequest;
import com.df4j.xctec.xcms.org.api.dto.UserOrgUnassignRequest;
import com.df4j.xctec.xcms.org.api.dto.UserPositionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户组织关系（管理面）。全 POST 风格，URL 为 /admin/user-org/{action}。
 */
@RestController
@RequestMapping("/api/admin/user-org")
@Tag(name = "用户组织关系 UserOrg", description = "管理面：用户-岗位-部门关系维护")
@RequiredArgsConstructor
public class UserOrgController {

    private final UserOrgService userOrgService;

    @Operation(summary = "分配用户到岗位", description = "建立用户-部门-岗位关系，可标记主岗位")
    @PostMapping("/assign")
    public ApiResponse<Void> assign(@RequestBody UserOrgAssignRequest request) {
        userOrgService.assignUserToPosition(
                request.getUserId(), request.getDeptId(), request.getPositionId(), request.isPrimary());
        return ApiResponse.success();
    }

    @Operation(summary = "取消用户岗位分配")
    @PostMapping("/unassign")
    public ApiResponse<Void> unassign(@RequestBody UserOrgUnassignRequest request) {
        userOrgService.removeUserFromPosition(request.getUserId(), request.getPositionId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询用户岗位列表")
    @PostMapping("/positions")
    public ApiResponse<List<UserPositionDTO>> getUserPositions(@RequestBody IdRequest request) {
        return ApiResponse.success(userOrgService.getUserPositions(request.getId()));
    }

    @Operation(summary = "查询用户主部门")
    @PostMapping("/primary-dept")
    public ApiResponse<DepartmentDTO> getUserPrimaryDept(@RequestBody IdRequest request) {
        return ApiResponse.success(userOrgService.getUserPrimaryDept(request.getId()));
    }

    @Operation(summary = "查询用户部门路径列表")
    @PostMapping("/paths")
    public ApiResponse<List<String>> getUserDeptPaths(@RequestBody IdRequest request) {
        return ApiResponse.success(userOrgService.getUserDeptPaths(request.getId()));
    }
}
