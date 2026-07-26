package com.df4j.xctec.xcms.identity.service;

import com.df4j.xctec.xcms.identity.api.SsoService;
import com.df4j.xctec.xcms.identity.api.dto.SsoAuthorizeDTO;
import com.df4j.xctec.xcms.identity.api.dto.SsoProviderDTO;
import com.df4j.xctec.xcms.identity.api.dto.request.SsoProviderCreateRequest;
import com.df4j.xctec.xcms.identity.api.dto.request.SsoProviderUpdateRequest;
import com.df4j.xctec.xcms.identity.domain.SsoProvider;
import com.df4j.xctec.xcms.identity.repository.SsoProviderRepository;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SsoServiceImpl implements SsoService {

    private final SsoProviderRepository providerRepository;

    @Override
    @Transactional
    public SsoProviderDTO createProvider(SsoProviderCreateRequest request) {
        if (providerRepository.existsByServerCode(request.getServerCode())) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, "serverCode 已存在: " + request.getServerCode());
        }
        SsoProvider p = new SsoProvider();
        p.setTenantId(request.getTenantId());
        apply(p, request.getServerCode(), request.getServerName(), request.getProtocol(),
                request.getClientId(), request.getClientSecret(), request.getAuthorizeUrl(),
                request.getTokenUrl(), request.getUserInfoUrl(), request.getRedirectUri(),
                request.getLogoutUrl(), request.getIdpUserIdField(), request.getUsernameField(),
                request.getEmailField(), request.getNameField(), request.getScope(),
                request.getAutoCreate(), request.getDefaultRole(), request.getEnabled());
        return toDto(providerRepository.save(p));
    }

    @Override
    @Transactional
    public SsoProviderDTO updateProvider(SsoProviderUpdateRequest request) {
        SsoProvider p = providerRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "SSO 服务不存在: " + request.getId()));
        if (request.getServerName() != null) {
            p.setServerName(request.getServerName());
        }
        if (request.getProtocol() != null) {
            p.setProtocol(request.getProtocol());
        }
        if (request.getClientId() != null) {
            p.setClientId(request.getClientId());
        }
        if (request.getClientSecret() != null) {
            p.setClientSecret(request.getClientSecret());
        }
        if (request.getAuthorizeUrl() != null) {
            p.setAuthorizeUrl(request.getAuthorizeUrl());
        }
        if (request.getTokenUrl() != null) {
            p.setTokenUrl(request.getTokenUrl());
        }
        if (request.getUserInfoUrl() != null) {
            p.setUserInfoUrl(request.getUserInfoUrl());
        }
        if (request.getRedirectUri() != null) {
            p.setRedirectUri(request.getRedirectUri());
        }
        if (request.getLogoutUrl() != null) {
            p.setLogoutUrl(request.getLogoutUrl());
        }
        if (request.getIdpUserIdField() != null) {
            p.setIdpUserIdField(request.getIdpUserIdField());
        }
        if (request.getUsernameField() != null) {
            p.setUsernameField(request.getUsernameField());
        }
        if (request.getEmailField() != null) {
            p.setEmailField(request.getEmailField());
        }
        if (request.getNameField() != null) {
            p.setNameField(request.getNameField());
        }
        if (request.getScope() != null) {
            p.setScope(request.getScope());
        }
        if (request.getAutoCreate() != null) {
            p.setAutoCreate(request.getAutoCreate());
        }
        if (request.getDefaultRole() != null) {
            p.setDefaultRole(request.getDefaultRole());
        }
        if (request.getEnabled() != null) {
            p.setEnabled(request.getEnabled());
        }
        return toDto(providerRepository.save(p));
    }

    @Override
    @Transactional
    public void deleteProvider(Long id) {
        if (!providerRepository.existsById(id)) {
            throw new BusinessException(ErrorCodes.NOT_FOUND, "SSO 服务不存在: " + id);
        }
        providerRepository.deleteById(id);
    }

    @Override
    public SsoProviderDTO getProvider(Long id) {
        return toDto(providerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "SSO 服务不存在: " + id)));
    }

    @Override
    public List<SsoProviderDTO> listProviders(Long tenantId) {
        List<SsoProvider> list = tenantId != null
                ? providerRepository.findByTenantId(tenantId) : providerRepository.findAll();
        return list.stream().map(this::toDto).toList();
    }

    @Override
    public SsoAuthorizeDTO authorize(String serverCode, String state) {
        SsoProvider p = providerRepository.findByServerCodeAndEnabledTrue(serverCode, true)
                .orElseThrow(() -> new BusinessException(ErrorCodes.BUSINESS_ERROR, "SSO 服务不可用: " + serverCode));
        SsoAuthorizeDTO dto = new SsoAuthorizeDTO();
        dto.setServerCode(serverCode);
        dto.setRedirectUrl(buildAuthorizeUrl(p, state));
        return dto;
    }

    private String buildAuthorizeUrl(SsoProvider p, String state) {
        StringBuilder sb = new StringBuilder(p.getAuthorizeUrl());
        sb.append(p.getAuthorizeUrl().contains("?") ? "&" : "?");
        sb.append("client_id=").append(enc(p.getClientId()));
        sb.append("&redirect_uri=").append(enc(p.getRedirectUri()));
        sb.append("&response_type=code");
        sb.append("&scope=").append(enc(p.getScope()));
        String st = (StringUtils.hasText(state) ? state : "") + ":" + UUID.randomUUID().toString().substring(0, 8);
        sb.append("&state=").append(enc(st));
        return sb.toString();
    }

    private String enc(String v) {
        return v == null ? "" : URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private void apply(SsoProvider p, String serverCode, String serverName, String protocol,
                       String clientId, String clientSecret, String authorizeUrl, String tokenUrl,
                       String userInfoUrl, String redirectUri, String logoutUrl,
                       String idpUserIdField, String usernameField, String emailField,
                       String nameField, String scope, Boolean autoCreate, String defaultRole, Boolean enabled) {
        p.setServerCode(serverCode);
        p.setServerName(serverName);
        p.setProtocol(protocol);
        p.setClientId(clientId);
        p.setClientSecret(clientSecret);
        p.setAuthorizeUrl(authorizeUrl);
        p.setTokenUrl(tokenUrl);
        p.setUserInfoUrl(userInfoUrl);
        p.setRedirectUri(redirectUri);
        p.setLogoutUrl(logoutUrl);
        p.setIdpUserIdField(idpUserIdField != null ? idpUserIdField : "sub");
        p.setUsernameField(usernameField != null ? usernameField : "email");
        p.setEmailField(emailField != null ? emailField : "email");
        p.setNameField(nameField != null ? nameField : "name");
        p.setScope(scope != null ? scope : "openid email profile");
        p.setAutoCreate(autoCreate != null ? autoCreate : true);
        p.setDefaultRole(defaultRole);
        p.setEnabled(enabled != null ? enabled : true);
    }

    private SsoProviderDTO toDto(SsoProvider p) {
        SsoProviderDTO dto = new SsoProviderDTO();
        dto.setId(p.getId());
        dto.setTenantId(p.getTenantId());
        dto.setServerCode(p.getServerCode());
        dto.setServerName(p.getServerName());
        dto.setProtocol(p.getProtocol());
        dto.setClientId(p.getClientId());
        dto.setAuthorizeUrl(p.getAuthorizeUrl());
        dto.setTokenUrl(p.getTokenUrl());
        dto.setUserInfoUrl(p.getUserInfoUrl());
        dto.setRedirectUri(p.getRedirectUri());
        dto.setLogoutUrl(p.getLogoutUrl());
        dto.setIdpUserIdField(p.getIdpUserIdField());
        dto.setUsernameField(p.getUsernameField());
        dto.setEmailField(p.getEmailField());
        dto.setNameField(p.getNameField());
        dto.setScope(p.getScope());
        dto.setAutoCreate(p.isAutoCreate());
        dto.setDefaultRole(p.getDefaultRole());
        dto.setEnabled(p.isEnabled());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }
}
