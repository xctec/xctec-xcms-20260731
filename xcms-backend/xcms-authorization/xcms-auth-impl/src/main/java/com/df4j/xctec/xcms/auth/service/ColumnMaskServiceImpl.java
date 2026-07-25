package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.api.ColumnMaskService;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskDTO;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskUpdateRequest;
import com.df4j.xctec.xcms.auth.domain.ColumnMask;
import com.df4j.xctec.xcms.auth.repository.ColumnMaskRepository;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColumnMaskServiceImpl implements ColumnMaskService {

    private final ColumnMaskRepository columnMaskRepository;

    @Override
    @Transactional
    public ColumnMaskDTO createMaskRule(ColumnMaskCreateRequest request) {
        requireTenant();
        ColumnMask mask = ColumnMask.builder()
                .resourceType(request.getResourceType())
                .fieldName(request.getFieldName())
                .maskType(request.getMaskType())
                .maskRule(request.getMaskRule())
                .roleIds(toString(request.getRoleIds()))
                .status("ACTIVE")
                .build();
        return toDTO(columnMaskRepository.save(mask));
    }

    @Override
    @Transactional
    public ColumnMaskDTO updateMaskRule(Long ruleId, ColumnMaskUpdateRequest request) {
        requireTenant();
        ColumnMask mask = columnMaskRepository.findById(ruleId)
                .filter(m -> m.getTenantId() != null && m.getTenantId().equals(TenantContext.getTenantId()))
                .orElseThrow(() -> new BusinessException(ErrorCodes.BUSINESS_ERROR, "脱敏规则不存在:" + ruleId));
        if (request.getResourceType() != null) mask.setResourceType(request.getResourceType());
        if (request.getFieldName() != null) mask.setFieldName(request.getFieldName());
        if (request.getMaskType() != null) mask.setMaskType(request.getMaskType());
        if (request.getMaskRule() != null) mask.setMaskRule(request.getMaskRule());
        if (request.getRoleIds() != null) mask.setRoleIds(toString(request.getRoleIds()));
        return toDTO(columnMaskRepository.save(mask));
    }

    @Override
    @Transactional
    public void deleteMaskRule(Long ruleId) {
        requireTenant();
        columnMaskRepository.deleteById(ruleId);
    }

    @Override
    public List<ColumnMaskDTO> listMaskRules(String resourceType) {
        requireTenant();
        return columnMaskRepository.findByResourceTypeAndTenantId(resourceType, TenantContext.getTenantId())
                .stream().map(this::toDTO).toList();
    }

    private ColumnMaskDTO toDTO(ColumnMask mask) {
        ColumnMaskDTO dto = new ColumnMaskDTO();
        dto.setId(mask.getId());
        dto.setResourceType(mask.getResourceType());
        dto.setFieldName(mask.getFieldName());
        dto.setMaskType(mask.getMaskType());
        dto.setMaskRule(mask.getMaskRule());
        dto.setRoleIds(toList(mask.getRoleIds()));
        return dto;
    }

    private String toString(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(ids.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    private List<Long> toList(String s) {
        if (s == null || s.isBlank()) {
            return new ArrayList<>();
        }
        String t = s.trim();
        if (t.startsWith("[")) {
            t = t.substring(1);
        }
        if (t.endsWith("]")) {
            t = t.substring(0, t.length() - 1);
        }
        t = t.trim();
        if (t.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> result = new ArrayList<>();
        for (String part : t.split(",")) {
            try {
                result.add(Long.valueOf(part.trim()));
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private void requireTenant() {
        if (TenantContext.getTenantId() == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "未确定租户上下文");
        }
    }
}
