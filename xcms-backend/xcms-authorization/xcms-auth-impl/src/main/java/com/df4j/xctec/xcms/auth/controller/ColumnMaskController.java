package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.ColumnMaskService;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskDTO;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskUpdateRequest;
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
 * 列级脱敏规则（管理面）。全 POST 风格，URL 为 /admin/column-mask/{action}。
 * 脱敏规则的"执行"由 ColumnMaskResponseBodyAdvice 在响应写出前自动完成。
 */
@RestController
@RequestMapping("/admin/column-mask")
@RequiredArgsConstructor
public class ColumnMaskController {

    private final ColumnMaskService columnMaskService;

    @PostMapping("/create")
    public ApiResponse<ColumnMaskDTO> createMaskRule(@RequestBody ColumnMaskCreateRequest request) {
        return ApiResponse.success(columnMaskService.createMaskRule(request));
    }

    @PostMapping("/update")
    public ApiResponse<ColumnMaskDTO> updateMaskRule(@RequestBody ColumnMaskUpdateRequest request) {
        return ApiResponse.success(columnMaskService.updateMaskRule(request.getId(), request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deleteMaskRule(@RequestBody IdRequest request) {
        columnMaskService.deleteMaskRule(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/list")
    public ApiResponse<List<ColumnMaskDTO>> listMaskRules(@RequestBody ResourceTypeRequest request) {
        return ApiResponse.success(columnMaskService.listMaskRules(request.getResourceType()));
    }
}
