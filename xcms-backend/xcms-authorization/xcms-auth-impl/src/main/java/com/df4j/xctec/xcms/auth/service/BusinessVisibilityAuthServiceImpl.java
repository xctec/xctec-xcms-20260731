package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.api.BusinessVisibilityAuthService;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthDTO;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthQuery;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthContext;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantAuthRequest;
import com.df4j.xctec.xcms.auth.api.event.CrossTenantAuthApprovedEvent;
import com.df4j.xctec.xcms.auth.domain.CrossTenantAuth;
import com.df4j.xctec.xcms.auth.repository.CrossTenantAuthRepository;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessVisibilityAuthServiceImpl implements BusinessVisibilityAuthService {

    private final CrossTenantAuthRepository repository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public CrossTenantAuthDTO requestAuthorization(CrossTenantAuthRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "未确定租户上下文");
        }
        CrossTenantAuth auth = CrossTenantAuth.builder()
                .tenantId(tenantId)
                .targetTenantId(request.getTargetTenantId())
                .userId(request.getUserId())
                .dataScope(request.getDataScope())
                .validUntil(request.getValidUntil())
                .status("PENDING")
                .reason(request.getReason())
                .build();
        return toDTO(repository.save(auth));
    }

    @Override
    @Transactional
    public void approveAuthorization(Long authId, Long approverId) {
        CrossTenantAuth auth = find(authId);
        auth.setStatus("ACTIVE");
        auth.setValidFrom(LocalDateTime.now());
        auth.setApprovedBy(approverId);
        auth.setToken(UUID.randomUUID().toString().replace("-", ""));
        auth = repository.save(auth);
        CrossTenantAuthApprovedEvent approvedEvent = new CrossTenantAuthApprovedEvent();
        approvedEvent.setAuthId(auth.getId());
        approvedEvent.setUserId(auth.getUserId());
        approvedEvent.setTenantId(auth.getTenantId());
        approvedEvent.setTargetTenantId(auth.getTargetTenantId());
        approvedEvent.setToken(auth.getToken());
        approvedEvent.setValidUntil(auth.getValidUntil());
        eventPublisher.publish(approvedEvent);
    }

    @Override
    @Transactional
    public void rejectAuthorization(Long authId, Long approverId, String reason) {
        CrossTenantAuth auth = find(authId);
        auth.setStatus("REJECTED");
        auth.setApprovedBy(approverId);
        auth.setReason(reason);
        repository.save(auth);
    }

    @Override
    @Transactional
    public void revokeAuthorization(Long authId) {
        CrossTenantAuth auth = find(authId);
        auth.setStatus("REVOKED");
        repository.save(auth);
    }

    @Override
    public PageResult<CrossTenantAuthDTO> listAuthorizations(CrossTenantAuthQuery query) {
        int page = query.getPage() <= 0 ? 1 : query.getPage();
        int size = query.getSize() <= 0 ? 20 : query.getSize();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CrossTenantAuth> result = repository.searchPage(
                query.getTenantId(), query.getTargetTenantId(), query.getUserId(), query.getStatus(), pageable);
        return PageResult.of(result.stream().map(this::toDTO).toList(), result.getTotalElements());
    }

    @Override
    public CrossTenantAuthDTO getActiveAuth(Long userId, Long targetTenantId) {
        List<CrossTenantAuth> list = repository.findByUserIdAndTargetTenantIdAndStatus(userId, targetTenantId, "ACTIVE");
        return list.isEmpty() ? null : toDTO(list.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public CrossTenantAuthContext verifyToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "令牌为空");
        }
        CrossTenantAuth a = repository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCodes.PERMISSION_DENIED, "令牌无效"));
        LocalDateTime now = LocalDateTime.now();
        if (!"ACTIVE".equals(a.getStatus())) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "授权未激活: " + a.getStatus());
        }
        if (a.getValidFrom() != null && now.isBefore(a.getValidFrom())) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "授权尚未生效");
        }
        if (a.getValidUntil() != null && now.isAfter(a.getValidUntil())) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "授权已过期");
        }
        CrossTenantAuthContext ctx = new CrossTenantAuthContext();
        ctx.setToken(token);
        ctx.setSourceTenantId(a.getTenantId());
        ctx.setTargetTenantId(a.getTargetTenantId());
        ctx.setUserId(a.getUserId());
        ctx.setDataScope(a.getDataScope());
        ctx.setValidFrom(a.getValidFrom());
        ctx.setValidUntil(a.getValidUntil());
        ctx.setStatus(a.getStatus());
        ctx.setValid(true);
        return ctx;
    }

    @Override
    @Transactional
    public void updateDataScope(Long authId, String dataScope) {
        CrossTenantAuth a = repository.findById(authId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "授权不存在: " + authId));
        a.setDataScope(dataScope);
        repository.save(a);
    }

    @Override
    @Transactional
    public int cleanupExpiredAuthorizations() {
        List<CrossTenantAuth> expired = repository.findByStatusAndValidUntilBefore("ACTIVE", LocalDateTime.now());
        for (CrossTenantAuth a : expired) {
            a.setStatus("EXPIRED");
        }
        repository.saveAll(expired);
        return expired.size();
    }

    private CrossTenantAuth find(Long authId) {
        return repository.findById(authId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.BUSINESS_ERROR, "授权记录不存在:" + authId));
    }

    private CrossTenantAuthDTO toDTO(CrossTenantAuth a) {
        CrossTenantAuthDTO dto = new CrossTenantAuthDTO();
        dto.setId(a.getId());
        dto.setTenantId(a.getTenantId());
        dto.setTargetTenantId(a.getTargetTenantId());
        dto.setUserId(a.getUserId());
        dto.setUserName(a.getUserName());
        dto.setDataScope(a.getDataScope());
        dto.setToken(a.getToken());
        dto.setValidFrom(a.getValidFrom());
        dto.setValidUntil(a.getValidUntil());
        dto.setStatus(a.getStatus());
        dto.setApprovedBy(a.getApprovedBy());
        dto.setReason(a.getReason());
        return dto;
    }
}
