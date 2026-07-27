package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.domain.ColumnMask;
import com.df4j.xctec.xcms.auth.domain.DataRule;
import com.df4j.xctec.xcms.auth.domain.DataRuleRole;
import com.df4j.xctec.xcms.auth.repository.ColumnMaskRepository;
import com.df4j.xctec.xcms.auth.repository.DataRuleRepository;
import com.df4j.xctec.xcms.auth.repository.DataRuleRoleRepository;
import com.df4j.xctec.xcms.datapermission.api.ColumnMaskRuleProvider;
import com.df4j.xctec.xcms.datapermission.api.ColumnMaskSpec;
import com.df4j.xctec.xcms.datapermission.api.DataPermissionRuleProvider;
import com.df4j.xctec.xcms.datapermission.api.DataRuleSpec;
import com.df4j.xctec.xcms.identity.api.RoleService;
import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限管理面规则提供者（AT-16 / AT-17）。
 *
 * <p>auth 模块保留规则的存储与管理（perm_data_rule / perm_column_mask），
 * 通过本 Provider 向 data-permission 模块供给运行时规则：
 * 用户 → 角色 → 规则绑定 → 规则的解析在此完成，data-permission 无需依赖 identity。</p>
 */
@Component
@RequiredArgsConstructor
public class AuthDataPermissionRuleProvider implements DataPermissionRuleProvider, ColumnMaskRuleProvider {

    private final RoleService roleService;
    private final DataRuleRoleRepository dataRuleRoleRepository;
    private final DataRuleRepository dataRuleRepository;
    private final ColumnMaskRepository columnMaskRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<DataRuleSpec> getRules(String resourceType, Long tenantId, Long userId) {
        if (tenantId == null || userId == null) {
            return List.of();
        }
        List<RoleDTO> roles = roleService.getUserRoles(userId);
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = roles.stream().map(RoleDTO::getId).toList();
        List<DataRuleRole> bindings = dataRuleRoleRepository.findByRoleIdIn(roleIds);
        if (bindings.isEmpty()) {
            return List.of();
        }
        List<Long> ruleIds = bindings.stream().map(DataRuleRole::getRuleId).toList();
        return dataRuleRepository.findByIdInAndTenantId(ruleIds, tenantId).stream()
                // 规则声明了 resourceType 时按资源过滤；为空视为全局规则
                .filter(r -> r.getResourceType() == null || r.getResourceType().equals(resourceType))
                .map(this::toSpec)
                .toList();
    }

    @Override
    public List<ColumnMaskSpec> getMasks(String resourceType, Long tenantId) {
        if (tenantId == null) {
            return List.of();
        }
        return columnMaskRepository.findByResourceTypeAndTenantId(resourceType, tenantId).stream()
                .filter(m -> "ACTIVE".equals(m.getStatus()))
                .map(this::toSpec)
                .toList();
    }

    private DataRuleSpec toSpec(DataRule rule) {
        return DataRuleSpec.builder()
                .dimension(rule.getDimension())
                .ruleConfig(rule.getRuleConfig())
                .priority(rule.getPriority())
                .status(rule.getStatus())
                .build();
    }

    private ColumnMaskSpec toSpec(ColumnMask mask) {
        return ColumnMaskSpec.builder()
                .fieldName(mask.getFieldName())
                .maskType(mask.getMaskType())
                .maskRule(mask.getMaskRule())
                .exemptIds(parseIds(mask.getRoleIds()))
                .build();
    }

    private List<Long> parseIds(String s) {
        if (s == null || s.isBlank()) {
            return List.of();
        }
        try {
            List<Long> parsed = objectMapper.readValue(s, new TypeReference<List<Long>>() {});
            return parsed == null ? List.of() : parsed;
        } catch (Exception e) {
            // 兼容非标准 JSON（如 "1,2,3"）的历史数据
            List<Long> result = new ArrayList<>();
            for (String id : s.split(",")) {
                try {
                    result.add(Long.valueOf(id.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
            return result;
        }
    }
}
