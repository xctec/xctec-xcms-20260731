package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.PermissionService;
import com.df4j.xctec.xcms.auth.api.dto.CheckPermissionRequest;
import com.df4j.xctec.xcms.auth.api.dto.MenuDTO;
import com.df4j.xctec.xcms.auth.api.dto.UserMenuRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
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
 * 权限（管理面）。全 POST 风格，URL 为 /admin/permission/{action}。
 */
@RestController
@RequestMapping("/admin/permission")
@Tag(name = "权限 Permission", description = "管理面：用户权限查询/鉴权/菜单树")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或令牌失效", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1201\",\"errorMsg\":\"未认证或令牌已失效，请重新登录\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无访问权限", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1203\",\"errorMsg\":\"无访问该资源的权限\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"5000\",\"errorMsg\":\"服务器内部错误，请稍后重试或联系管理员\",\"data\":null}")))
public class PermissionController {

    private final PermissionService permissionService;

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回权限编码列表", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":[\"user:read\",\"user:write\",\"role:assign\"]}")))
    @Operation(summary = "查询用户权限", description = "返回用户拥有的权限编码列表。")
    @PostMapping("/user-permissions")
    public ApiResponse<List<String>> getUserPermissions(@RequestBody IdRequest request) {
        return ApiResponse.success(permissionService.getUserPermissions(request.getId()).stream().toList());
    }

    @Operation(summary = "鉴权校验", description = "判断用户是否拥有指定权限编码。")
    @PostMapping("/check")
    public ApiResponse<Boolean> checkPermission(@RequestBody CheckPermissionRequest request) {
        return ApiResponse.success(permissionService.checkPermission(request.getUserId(), request.getPermCode()));
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回用户菜单树", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":[{\"id\":1,\"menuName\":\"系统管理\",\"children\":[{\"id\":2,\"menuName\":\"用户管理\"}]}]}")))
    @Operation(summary = "查询用户菜单", description = "返回用户在指定菜单范围（scope）下的菜单树。")
    @PostMapping("/menus")
    public ApiResponse<List<MenuDTO>> getUserMenus(@RequestBody UserMenuRequest request) {
        return ApiResponse.success(permissionService.getUserMenus(
                request.getUserId(), com.df4j.xctec.xcms.auth.api.enums.MenuScope.valueOf(request.getScope())));
    }
}
