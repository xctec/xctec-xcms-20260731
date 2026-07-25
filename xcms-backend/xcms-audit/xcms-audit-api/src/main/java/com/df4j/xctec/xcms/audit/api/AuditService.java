package com.df4j.xctec.xcms.audit.api;

import com.df4j.xctec.xcms.audit.api.dto.AuditLogDTO;
import com.df4j.xctec.xcms.audit.api.dto.AuditQuery;
import com.df4j.xctec.xcms.kernel.common.PageResult;

/**
 * 审计查询服务（管理面）。
 */
public interface AuditService {
    PageResult<AuditLogDTO> query(AuditQuery query);
}
