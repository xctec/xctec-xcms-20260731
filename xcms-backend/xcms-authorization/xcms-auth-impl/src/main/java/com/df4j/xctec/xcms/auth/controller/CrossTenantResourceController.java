package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.CrossTenantResourceService;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantResourceDTO;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceQuery;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceUpdateRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/cross-tenant-resource")
@RequiredArgsConstructor
public class CrossTenantResourceController {

    private final CrossTenantResourceService resourceService;

    @PostMapping("/create")
    public ApiResponse<CrossTenantResourceDTO> create(@RequestBody CrossTenantResourceCreateRequest request) {
        return ApiResponse.success(resourceService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<CrossTenantResourceDTO> update(@RequestBody CrossTenantResourceUpdateRequest request) {
        return ApiResponse.success(resourceService.update(request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdRequest request) {
        resourceService.delete(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/get")
    public ApiResponse<CrossTenantResourceDTO> get(@RequestBody IdRequest request) {
        return ApiResponse.success(resourceService.get(request.getId()));
    }

    @PostMapping("/list")
    public ApiResponse<PageResult<CrossTenantResourceDTO>> list(@RequestBody CrossTenantResourceQuery query) {
        return ApiResponse.success(resourceService.list(query));
    }
}
