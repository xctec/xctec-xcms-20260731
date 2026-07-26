package com.df4j.xctec.xcms.auth.api;

import com.df4j.xctec.xcms.auth.api.dto.CrossTenantResourceDTO;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceQuery;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceUpdateRequest;
import com.df4j.xctec.xcms.kernel.common.PageResult;

/**
 * 跨租户可见资源目录管理（resource_type=DATA 时的可见资源集合）。
 */
public interface CrossTenantResourceService {

    CrossTenantResourceDTO create(CrossTenantResourceCreateRequest request);

    CrossTenantResourceDTO update(CrossTenantResourceUpdateRequest request);

    void delete(Long id);

    CrossTenantResourceDTO get(Long id);

    PageResult<CrossTenantResourceDTO> list(CrossTenantResourceQuery query);
}
