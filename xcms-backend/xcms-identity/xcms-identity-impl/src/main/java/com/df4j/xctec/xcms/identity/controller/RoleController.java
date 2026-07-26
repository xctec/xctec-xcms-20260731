package com.df4j.xctec.xcms.identity.controller;

import com.df4j.xctec.xcms.identity.api.RoleService;
import com.df4j.xctec.xcms.identity.api.dto.RoleCreateRequest;
import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import com.df4j.xctec.xcms.identity.api.dto.RoleQuery;
import com.df4j.xctec.xcms.identity.api.dto.RoleScopeRequest;
import com.df4j.xctec.xcms.identity.api.dto.RoleUpdateRequest;
import com.df4j.xctec.xcms.identity.api.enums.RoleScope;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
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
 * 角色管理（管理面）。全 POST 风格，URL 为 /admin/role/{action}。
 */
@RestController
@RequestMapping("/admin/role")
@Tag(name = "角色管理 Role", description = "管理面：角色创建/查询/按授权范围列举")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或令牌失效", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1201\",\"errorMsg\":\"未认证或令牌已失效，请重新登录\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无访问权限", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1203\",\"errorMsg\":\"无访问该资源的权限\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"5000\",\"errorMsg\":\"服务器内部错误，请稍后重试或联系管理员\",\"data\":null}")))
public class RoleController {

    private final RoleService roleService;

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功，返回新角色信息", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"id\":1,\"roleCode\":\"auditor\",\"roleName\":\"审计员\",\"scope\":\"TENANT\"}}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数校验失败（如角色编码重复）", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1000\",\"errorMsg\":\"参数校验失败：角色编码已存在\",\"data\":null}")))
    @Operation(summary = "创建角色", description = "创建角色定义。")
    @PostMapping("/create")
    public ApiResponse<RoleDTO> createRole(@RequestBody RoleCreateRequest request) {
        return ApiResponse.success(roleService.createRole(request));
    }

    @Operation(summary = "更新角色", description = "按 id 更新角色。")
    @PostMapping("/update")
    public ApiResponse<RoleDTO> updateRole(@RequestBody RoleUpdateRequest request) {
        return ApiResponse.success(roleService.updateRole(request.getId(), request));
    }

    @Operation(summary = "删除角色", description = "删除角色（需先解绑用户）。")
    @PostMapping("/delete")
    public ApiResponse<Void> deleteRole(@RequestBody IdRequest request) {
        roleService.deleteRole(request.getId());
        return ApiResponse.success();
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回角色详情", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"id\":1,\"roleCode\":\"auditor\",\"roleName\":\"审计员\",\"scope\":\"TENANT\"}}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "角色不存在", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1004\",\"errorMsg\":\"角色不存在\",\"data\":null}")))
    @Operation(summary = "查询角色详情", description = "按 id 查询角色。")
    @PostMapping("/get")
    public ApiResponse<RoleDTO> getRole(@RequestBody IdRequest request) {
        return ApiResponse.success(roleService.getRole(request.getId()));
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回分页角色列表", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"list\":[{\"id\":1,\"roleCode\":\"auditor\",\"roleName\":\"审计员\"}],\"total\":10,\"page\":1,\"size\":20}}")))
    @Operation(summary = "分页查询角色", description = "按条件分页查询角色。")
    @PostMapping("/list")
    public ApiResponse<PageResult<RoleDTO>> listRoles(@RequestBody RoleQuery query) {
        return ApiResponse.success(roleService.listRoles(query));
    }

    @Operation(summary = "按授权范围列举角色", description = "返回指定授权范围（TENANT/DEPT/CUSTOM）下的角色。")
    @PostMapping("/list-by-scope")
    public ApiResponse<List<RoleDTO>> listByScope(@RequestBody RoleScopeRequest request) {
        return ApiResponse.success(roleService.listByScope(RoleScope.valueOf(request.getScope())));
    }
}
