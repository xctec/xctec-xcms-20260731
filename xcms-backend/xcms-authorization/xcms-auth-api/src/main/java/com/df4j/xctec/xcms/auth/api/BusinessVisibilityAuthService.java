package com.df4j.xctec.xcms.auth.api;

import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthContext;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthDTO;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthQuery;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthRequest;
import com.df4j.xctec.xcms.kernel.common.PageResult;

public interface BusinessVisibilityAuthService {

    CrossTenantAuthDTO requestAuthorization(CrossTenantAuthRequest request);

    void approveAuthorization(Long authId, Long approverId);

    void rejectAuthorization(Long authId, Long approverId, String reason);

    void revokeAuthorization(Long authId);

    PageResult<CrossTenantAuthDTO> listAuthorizations(CrossTenantAuthQuery query);

    CrossTenantAuthDTO getActiveAuth(Long userId, Long targetTenantId);

    /** 校验令牌有效性，返回授权上下文（含 dataScope 可见资源范围） */
    CrossTenantAuthContext verifyToken(String token);

    /** 更新授权的数据范围（dataScope JSON） */
    void updateDataScope(Long authId, String dataScope);

    /** 清理过期的授权记录，返回清理数量 */
    int cleanupExpiredAuthorizations();
}
