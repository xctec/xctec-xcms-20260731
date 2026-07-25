package com.df4j.xctec.xcms.org.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.org.api.PositionService;
import com.df4j.xctec.xcms.org.api.dto.PositionCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.PositionDTO;
import com.df4j.xctec.xcms.org.api.dto.PositionUpdateRequest;
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
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping("/create")
    public ApiResponse<PositionDTO> createPosition(@RequestBody PositionCreateRequest request) {
        return ApiResponse.success(positionService.createPosition(request));
    }

    @PostMapping("/update")
    public ApiResponse<PositionDTO> updatePosition(@RequestBody PositionUpdateRequest request) {
        return ApiResponse.success(positionService.updatePosition(request.getId(), request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deletePosition(@RequestBody IdRequest request) {
        positionService.deletePosition(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/list-by-dept")
    public ApiResponse<List<PositionDTO>> listPositionsByDept(@RequestBody IdRequest request) {
        return ApiResponse.success(positionService.listPositionsByDept(request.getId()));
    }
}
