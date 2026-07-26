package com.df4j.xctec.xcms.auth.controller;

import com.df4j.xctec.xcms.auth.api.ColumnMaskService;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskDTO;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskUpdateRequest;
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
 * 列级脱敏规则（管理面）。全 POST 风格，URL 为 /admin/column-mask/{action}。
 * 脱敏规则的"执行"由 ColumnMaskResponseBodyAdvice 在响应写出前自动完成。
 */
@RestController
@RequestMapping("/admin/column-mask")
@Tag(name = "列级脱敏 ColumnMask", description = "管理面：列级脱敏规则 CRUD（执行由响应拦截器自动完成）")
@RequiredArgsConstructor
public class ColumnMaskController {

    private final ColumnMaskService columnMaskService;

    @Operation(summary = "创建脱敏规则", description = "新建列级脱敏规则（resourceType 标识作用对象）。")
    @PostMapping("/create")
    public ApiResponse<ColumnMaskDTO> createMaskRule(@RequestBody ColumnMaskCreateRequest request) {
        return ApiResponse.success(columnMaskService.createMaskRule(request));
    }

    @Operation(summary = "更新脱敏规则", description = "按 id 更新脱敏规则。")
    @PostMapping("/update")
    public ApiResponse<ColumnMaskDTO> updateMaskRule(@RequestBody ColumnMaskUpdateRequest request) {
        return ApiResponse.success(columnMaskService.updateMaskRule(request.getId(), request));
    }

    @Operation(summary = "删除脱敏规则", description = "删除脱敏规则。")
    @PostMapping("/delete")
    public ApiResponse<Void> deleteMaskRule(@RequestBody IdRequest request) {
        columnMaskService.deleteMaskRule(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询脱敏规则", description = "按资源类型查询脱敏规则列表。")
    @PostMapping("/list")
    public ApiResponse<List<ColumnMaskDTO>> listMaskRules(@RequestBody ResourceTypeRequest request) {
        return ApiResponse.success(columnMaskService.listMaskRules(request.getResourceType()));
    }
}
