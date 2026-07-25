package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.DataPermissionContext;
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
@RequiredArgsConstructor
public class DataRuleController {

    private final DataRuleService dataRuleService;

    @PostMapping("/create")
    public ApiResponse<DataRuleDTO> createRule(@RequestBody DataRuleCreateRequest request) {
        return ApiResponse.success(dataRuleService.createRule(request));
    }

    @PostMapping("/update")
    public ApiResponse<DataRuleDTO> updateRule(@RequestBody DataRuleUpdateRequest request) {
        return ApiResponse.success(dataRuleService.updateRule(request.getId(), request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deleteRule(@RequestBody IdRequest request) {
        dataRuleService.deleteRule(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/list")
    public ApiResponse<List<DataRuleDTO>> listRules(@RequestBody ResourceTypeRequest request) {
        return ApiResponse.success(dataRuleService.listRules(request.getResourceType()));
    }

    @PostMapping("/bind")
    public ApiResponse<Void> bindRuleToRole(@RequestBody DataRuleBindRequest request) {
        dataRuleService.bindRuleToRole(request.getId(), request.getRoleId(), request.getScopeValue());
        return ApiResponse.success();
    }

    @PostMapping("/unbind")
    public ApiResponse<Void> unbindRuleFromRole(@RequestBody DataRuleUnbindRequest request) {
        dataRuleService.unbindRuleFromRole(request.getId(), request.getRoleId());
        return ApiResponse.success();
    }

    @PostMapping("/test")
    public ApiResponse<DataPermissionContext> testPermission(@RequestBody DataRuleTestRequest request) {
        return ApiResponse.success(dataRuleService.testPermission(request.getUserId(), request.getResourceType()));
    }
}
