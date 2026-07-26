package com.df4j.xctec.xcms.identity.api;

import com.df4j.xctec.xcms.identity.api.dto.SsoAuthorizeDTO;
import com.df4j.xctec.xcms.identity.api.dto.SsoProviderDTO;
import com.df4j.xctec.xcms.identity.api.dto.request.SsoProviderCreateRequest;
import com.df4j.xctec.xcms.identity.api.dto.request.SsoProviderUpdateRequest;

import java.util.List;

/**
 * SSO 服务：服务端管理与授权跳转。
 */
public interface SsoService {

    SsoProviderDTO createProvider(SsoProviderCreateRequest request);

    SsoProviderDTO updateProvider(SsoProviderUpdateRequest request);

    void deleteProvider(Long id);

    SsoProviderDTO getProvider(Long id);

    List<SsoProviderDTO> listProviders(Long tenantId);

    /** 根据 serverCode 生成 IdP 授权跳转地址 */
    SsoAuthorizeDTO authorize(String serverCode, String state);
}
