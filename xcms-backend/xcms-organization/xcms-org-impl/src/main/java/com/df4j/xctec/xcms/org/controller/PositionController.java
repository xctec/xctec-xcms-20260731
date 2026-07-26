package com.df4j.xctec.xcms.org.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.org.api.PositionService;
import com.df4j.xctec.xcms.org.api.dto.PositionCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.PositionDTO;
import com.df4j.xctec.xcms.org.api.dto.PositionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 岗位管理（管理面）。全 POST 风格，URL 为 /admin/position/{action}。
 */
@RestController
@RequestMapping("/admin/position")
@Tag(name = "岗位 Position", description = "管理面：岗位创建/更新/按部门列举")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @Operation(summary = "创建岗位")
    @PostMapping("/create")
    public ApiResponse<PositionDTO> createPosition(@RequestBody PositionCreateRequest request) {
        return ApiResponse.success(positionService.createPosition(request));
    }

    @Operation(summary = "更新岗位")
    @PostMapping("/update")
    public ApiResponse<PositionDTO> updatePosition(@RequestBody PositionUpdateRequest request) {
        return ApiResponse.success(positionService.updatePosition(request.getId(), request));
    }

    @Operation(summary = "删除岗位")
    @PostMapping("/delete")
    public ApiResponse<Void> deletePosition(@RequestBody IdRequest request) {
        positionService.deletePosition(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "按部门查询岗位列表")
    @PostMapping("/list-by-dept")
    public ApiResponse<List<PositionDTO>> listPositionsByDept(@RequestBody IdRequest request) {
        return ApiResponse.success(positionService.listPositionsByDept(request.getId()));
    }
}
