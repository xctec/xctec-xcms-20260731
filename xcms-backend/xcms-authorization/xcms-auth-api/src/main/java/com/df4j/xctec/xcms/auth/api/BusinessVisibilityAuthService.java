package com.df4j.xctec.xcms.auth.api;

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

    void cleanupExpiredAuthorizations();
}
