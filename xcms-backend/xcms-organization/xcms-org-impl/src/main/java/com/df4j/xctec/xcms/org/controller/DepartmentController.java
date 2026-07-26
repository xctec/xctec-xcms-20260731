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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "部门 Department", description = "管理面：部门创建/层级/成员管理")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或令牌失效", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1201\",\"errorMsg\":\"未认证或令牌已失效，请重新登录\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无访问权限", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1203\",\"errorMsg\":\"无访问该资源的权限\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"5000\",\"errorMsg\":\"服务器内部错误，请稍后重试或联系管理员\",\"data\":null}")))
public class DepartmentController {

    private final OrganizationService organizationService;

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功，返回新部门信息", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"id\":1,\"deptCode\":\"rd\",\"deptName\":\"研发部\",\"parentId\":null}}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数校验失败", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1000\",\"errorMsg\":\"参数校验失败：上级部门不存在\",\"data\":null}")))
    @Operation(summary = "创建部门")
    @PostMapping("/create")
    public ApiResponse<DepartmentDTO> createDepartment(@RequestBody DepartmentCreateRequest request) {
        return ApiResponse.success(organizationService.createDepartment(request));
    }

    @Operation(summary = "更新部门")
    @PostMapping("/update")
    public ApiResponse<DepartmentDTO> updateDepartment(@RequestBody DepartmentUpdateRequest request) {
        return ApiResponse.success(organizationService.updateDepartment(request.getId(), request));
    }

    @Operation(summary = "删除部门")
    @PostMapping("/delete")
    public ApiResponse<Void> deleteDepartment(@RequestBody IdRequest request) {
        organizationService.deleteDepartment(request.getId());
        return ApiResponse.success();
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回部门详情", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"id\":1,\"deptCode\":\"rd\",\"deptName\":\"研发部\",\"parentId\":null,\"children\":[]}}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "部门不存在", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1004\",\"errorMsg\":\"部门不存在\",\"data\":null}")))
    @Operation(summary = "查询部门详情")
    @PostMapping("/get")
    public ApiResponse<DepartmentDTO> getDepartment(@RequestBody IdRequest request) {
        return ApiResponse.success(organizationService.getDepartment(request.getId()));
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回部门层级树", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":[{\"id\":1,\"deptName\":\"总公司\",\"children\":[{\"id\":2,\"deptName\":\"研发部\"}]}]}")))
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
