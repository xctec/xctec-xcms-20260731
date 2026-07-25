package com.df4j.xctec.xcms.audit.controller;

import com.df4j.xctec.xcms.audit.api.AuditService;
import com.df4j.xctec.xcms.audit.api.dto.AuditLogDTO;
import com.df4j.xctec.xcms.audit.api.dto.AuditQuery;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
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
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping("/query")
    public ApiResponse<PageResult<AuditLogDTO>> query(@RequestBody AuditQuery query) {
        return ApiResponse.success(auditService.query(query));
    }
}
