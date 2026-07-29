package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.CrossTenantResourceService;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantResourceDTO;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceQuery;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceUpdateRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cross-tenant-resource")
@Tag(name = "跨租户资源 CrossTenantResource", description = "管理面：跨租户共享资源的 CRUD 与查询")
@RequiredArgsConstructor
public class CrossTenantResourceController {

    private final CrossTenantResourceService resourceService;

    @Operation(summary = "创建跨租户资源", description = "创建一条跨租户共享资源定义。")
    @PostMapping("/create")
    public ApiResponse<CrossTenantResourceDTO> create(@RequestBody CrossTenantResourceCreateRequest request) {
        return ApiResponse.success(resourceService.create(request));
    }

    @Operation(summary = "更新跨租户资源", description = "按 id 更新资源定义。")
    @PostMapping("/update")
    public ApiResponse<CrossTenantResourceDTO> update(@RequestBody CrossTenantResourceUpdateRequest request) {
        return ApiResponse.success(resourceService.update(request));
    }

    @Operation(summary = "删除跨租户资源", description = "删除资源定义。")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdRequest request) {
        resourceService.delete(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询资源详情", description = "按 id 查询资源。")
    @PostMapping("/get")
    public ApiResponse<CrossTenantResourceDTO> get(@RequestBody IdRequest request) {
        return ApiResponse.success(resourceService.get(request.getId()));
    }

    @Operation(summary = "分页查询资源", description = "按条件分页查询跨租户资源。")
    @PostMapping("/list")
    public ApiResponse<PageResult<CrossTenantResourceDTO>> list(@RequestBody CrossTenantResourceQuery query) {
        return ApiResponse.success(resourceService.list(query));
    }
}
