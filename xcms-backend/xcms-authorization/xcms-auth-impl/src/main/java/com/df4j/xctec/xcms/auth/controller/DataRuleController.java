package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.datapermission.api.DataPermissionContext;
import com.df4j.xctec.xcms.auth.api.DataRuleService;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleDTO;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleBindRequest;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleTestRequest;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleUnbindRequest;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleUpdateRequest;
import com.df4j.xctec.xcms.auth.api.dto.ResourceTypeRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据规则（管理面）。全 POST 风格，URL 为 /admin/data-rule/{action}。
 */
@RestController
@RequestMapping("/admin/data-rule")
@Tag(name = "数据规则 DataRule", description = "管理面：数据权限规则 CRUD/绑定角色/测试")
@RequiredArgsConstructor
public class DataRuleController {

    private final DataRuleService dataRuleService;

    @Operation(summary = "创建数据规则", description = "新建一条数据权限规则。")
    @PostMapping("/create")
    public ApiResponse<DataRuleDTO> createRule(@RequestBody DataRuleCreateRequest request) {
        return ApiResponse.success(dataRuleService.createRule(request));
    }

    @Operation(summary = "更新数据规则", description = "按 id 更新数据规则。")
    @PostMapping("/update")
    public ApiResponse<DataRuleDTO> updateRule(@RequestBody DataRuleUpdateRequest request) {
        return ApiResponse.success(dataRuleService.updateRule(request.getId(), request));
    }

    @Operation(summary = "删除数据规则", description = "删除数据规则。")
    @PostMapping("/delete")
    public ApiResponse<Void> deleteRule(@RequestBody IdRequest request) {
        dataRuleService.deleteRule(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询资源类型规则列表", description = "按资源类型查询数据规则。")
    @PostMapping("/list")
    public ApiResponse<List<DataRuleDTO>> listRules(@RequestBody ResourceTypeRequest request) {
        return ApiResponse.success(dataRuleService.listRules(request.getResourceType()));
    }

    @Operation(summary = "绑定规则到角色", description = "将数据规则绑定到角色并记录作用范围。")
    @PostMapping("/bind")
    public ApiResponse<Void> bindRuleToRole(@RequestBody DataRuleBindRequest request) {
        dataRuleService.bindRuleToRole(request.getId(), request.getRoleId(), request.getScopeValue());
        return ApiResponse.success();
    }

    @Operation(summary = "解绑角色规则", description = "解除数据规则与角色的绑定。")
    @PostMapping("/unbind")
    public ApiResponse<Void> unbindRuleFromRole(@RequestBody DataRuleUnbindRequest request) {
        dataRuleService.unbindRuleFromRole(request.getId(), request.getRoleId());
        return ApiResponse.success();
    }

    @Operation(summary = "测试数据权限", description = "按用户与资源类型返回解析后的数据权限上下文。")
    @PostMapping("/test")
    public ApiResponse<DataPermissionContext> testPermission(@RequestBody DataRuleTestRequest request) {
        return ApiResponse.success(dataRuleService.testPermission(request.getUserId(), request.getResourceType()));
    }
}
