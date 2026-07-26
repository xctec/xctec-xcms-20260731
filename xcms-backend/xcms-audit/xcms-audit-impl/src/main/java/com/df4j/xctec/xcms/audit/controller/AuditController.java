package com.df4j.xctec.xcms.audit.controller;

import com.df4j.xctec.xcms.audit.api.AuditService;
import com.df4j.xctec.xcms.audit.api.dto.AuditLogDTO;
import com.df4j.xctec.xcms.audit.api.dto.AuditQuery;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计查询（管理面）。全 POST 风格。
 */
@RestController
@RequestMapping("/admin/audit")
@Tag(name = "审计 Audit", description = "管理面：审计日志查询")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "查询审计日志", description = "按条件分页查询审计日志")
    @PostMapping("/query")
    public ApiResponse<PageResult<AuditLogDTO>> query(@RequestBody AuditQuery query) {
        return ApiResponse.success(auditService.query(query));
    }
}
