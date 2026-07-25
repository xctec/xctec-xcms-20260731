package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.api.DataPermissionService;
import com.df4j.xctec.xcms.auth.api.DataRuleService;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleDTO;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleUpdateRequest;
import com.df4j.xctec.xcms.auth.domain.DataRule;
import com.df4j.xctec.xcms.auth.repository.DataRuleRepository;
import com.df4j.xctec.xcms.auth.repository.DataRuleRoleRepository;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataRuleServiceImpl implements DataRuleService {

    private final DataRuleRepository dataRuleRepository;
    private final DataRuleRoleRepository dataRuleRoleRepository;
    private final DataPermissionService dataPermissionService;

    @Override
    @Transactional
    public DataRuleDTO createRule(DataRuleCreateRequest request) {
        requireTenant();
        DataRule rule = DataRule.builder()
                .ruleName(request.getRuleName())
                .ruleType(request.getRuleType())
                .resourceType(request.getResourceType())
                .dimension(request.getDimension())
                .ruleConfig(request.getRuleConfig())
                .priority(request.getPriority() == null ? 0 : request.getPriority())
                .status("ACTIVE")
                .build();
        return toDTO(dataRuleRepository.save(rule));
    }

    @Override
    @Transactional
    public DataRuleDTO updateRule(Long ruleId, DataRuleUpdateRequest request) {
        requireTenant();
        DataRule rule = findRule(ruleId);
        if (request.getRuleName() != null) rule.setRuleName(request.getRuleName());
        if (request.getRuleConfig() != null) rule.setRuleConfig(request.getRuleConfig());
        if (request.getPriority() != null) rule.setPriority(request.getPriority());
        if (request.getStatus() != null) rule.setStatus(request.getStatus());
        return toDTO(dataRuleRepository.save(rule));
    }

    @Override
    @Transactional
    public void deleteRule(Long ruleId) {
        requireTenant();
        findRule(ruleId);
        dataRuleRoleRepository.deleteByRuleId(ruleId);
        dataRuleRepository.deleteById(ruleId);
    }

    @Override
    public List<DataRuleDTO> listRules(String resourceType) {
        requireTenant();
        return dataRuleRepository.findByResourceTypeAndTenantId(resourceType, TenantContext.getTenantId())
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public void bindRuleToRole(Long ruleId, Long roleId, String scopeValue) {
        requireTenant();
        findRule(ruleId);
        boolean alreadyBound = dataRuleRoleRepository.findByRuleId(ruleId).stream()
                .anyMatch(d -> d.getRoleId().equals(roleId));
        if (!alreadyBound) {
            dataRuleRoleRepository.save(com.df4j.xctec.xcms.auth.domain.DataRuleRole.builder()
                    .ruleId(ruleId).roleId(roleId).scopeValue(scopeValue).build());
        }
    }

    @Override
    @Transactional
    public void unbindRuleFromRole(Long ruleId, Long roleId) {
        requireTenant();
        dataRuleRoleRepository.deleteByRuleIdAndRoleId(ruleId, roleId);
    }

    @Override
    public com.df4j.xctec.xcms.auth.api.DataPermissionContext testPermission(Long userId, String resourceType) {
        return dataPermissionService.getDataPermissionContext(userId, resourceType);
    }

    private DataRuleDTO toDTO(DataRule rule) {
        DataRuleDTO dto = new DataRuleDTO();
        dto.setId(rule.getId());
        dto.setRuleName(rule.getRuleName());
        dto.setRuleType(rule.getRuleType());
        dto.setResourceType(rule.getResourceType());
        dto.setDimension(rule.getDimension());
        dto.setRuleConfig(rule.getRuleConfig());
        dto.setPriority(rule.getPriority());
        dto.setStatus(rule.getStatus());
        return dto;
    }

    private DataRule findRule(Long ruleId) {
        return dataRuleRepository.findById(ruleId)
                .filter(r -> r.getTenantId() != null && r.getTenantId().equals(TenantContext.getTenantId()))
                .orElseThrow(() -> new BusinessException(ErrorCodes.BUSINESS_ERROR, "数据规则不存在:" + ruleId));
    }

    private void requireTenant() {
        if (TenantContext.getTenantId() == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "未确定租户上下文");
        }
    }
}
