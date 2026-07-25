package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.api.DataPermissionContext;
import com.df4j.xctec.xcms.auth.api.DataPermissionService;
import com.df4j.xctec.xcms.auth.domain.ColumnMask;
import com.df4j.xctec.xcms.auth.domain.DataRule;
import com.df4j.xctec.xcms.auth.domain.DataRuleRole;
import com.df4j.xctec.xcms.auth.repository.ColumnMaskRepository;
import com.df4j.xctec.xcms.auth.repository.DataRuleRepository;
import com.df4j.xctec.xcms.auth.repository.DataRuleRoleRepository;
import com.df4j.xctec.xcms.auth.repository.RolePermissionRepository;
import com.df4j.xctec.xcms.identity.api.RoleService;
import com.df4j.xctec.xcms.identity.api.UserService;
import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import jakarta.persistence.Entity;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataPermissionServiceImpl implements DataPermissionService {

    private final UserService userService;
    private final RoleService roleService;
    private final RolePermissionRepository rolePermissionRepository;
    private final DataRuleRoleRepository dataRuleRoleRepository;
    private final DataRuleRepository dataRuleRepository;
    private final ColumnMaskRepository columnMaskRepository;
    private final ObjectMapper objectMapper;

    @Override
    public <T> Specification<T> getDataScopeSpec(Long userId, String resourceType) {
        DataPermissionContext ctx = getDataPermissionContext(userId, resourceType);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ctx.isOwnerOnly() && ctx.getOwnerField() != null) {
                predicates.add(cb.equal(root.get(ctx.getOwnerField()), userId));
            }
            if (ctx.hasOrgScope()) {
                List<Predicate> ors = new ArrayList<>();
                for (String p : ctx.getOrgPaths()) {
                    ors.add(cb.like(root.<String>get("orgPath"), p + "%"));
                }
                predicates.add(cb.or(ors.toArray(new Predicate[0])));
            }
            if (ctx.hasBusinessLineScope()) {
                predicates.add(root.<Long>get("businessLineId").in(ctx.getBusinessLineIds()));
            }
            if (ctx.hasRegionScope()) {
                predicates.add(root.<String>get("region").in(ctx.getRegions()));
            }
            if (ctx.hasTagScope()) {
                predicates.add(root.<String>get("tags").in(ctx.getTags()));
            }
            if (ctx.hasTimeScope()) {
                if (ctx.getTimeFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("createdAt"), ctx.getTimeFrom()));
                }
                if (ctx.getTimeTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("createdAt"), ctx.getTimeTo()));
                }
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public DataPermissionContext getDataPermissionContext(Long userId, String resourceType) {
        Long tenantId = TenantContext.getTenantId();
        List<RoleDTO> roles = roleService.getUserRoles(userId);
        DataPermissionContext.Builder builder = DataPermissionContext.builder()
                .userId(userId).tenantId(tenantId).resourceType(resourceType);
        if (roles == null || roles.isEmpty()) {
            return builder.build();
        }
        List<Long> roleIds = roles.stream().map(RoleDTO::getId).toList();
        List<DataRuleRole> drs = dataRuleRoleRepository.findByRoleIdIn(roleIds);
        if (drs.isEmpty()) {
            return builder.build();
        }
        List<Long> ruleIds = drs.stream().map(DataRuleRole::getRuleId).toList();
        List<DataRule> rules = dataRuleRepository.findByIdInAndTenantId(ruleIds, tenantId);
        List<String> orgPaths = new ArrayList<>();
        List<Long> businessLineIds = new ArrayList<>();
        List<String> regions = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        LocalDateTime timeFrom = null;
        LocalDateTime timeTo = null;
        boolean ownerOnly = false;
        for (DataRule rule : rules) {
            if (!"ACTIVE".equals(rule.getStatus()) || rule.getDimension() == null) {
                continue;
            }
            switch (rule.getDimension()) {
                case "OWNER" -> { ownerOnly = true; builder.ownerField("createdBy"); }
                case "ORG" -> { if (rule.getRuleConfig() != null) orgPaths.add(rule.getRuleConfig()); }
                case "BUSINESS_LINE" -> parseLongs(rule.getRuleConfig(), businessLineIds);
                case "REGION" -> parseStrings(rule.getRuleConfig(), regions);
                case "TAG" -> parseStrings(rule.getRuleConfig(), tags);
                case "TIME" -> {
                    String[] parts = rule.getRuleConfig() == null ? new String[0] : rule.getRuleConfig().split(",");
                    if (parts.length > 0 && !parts[0].isBlank()) timeFrom = parseTime(parts[0]);
                    if (parts.length > 1 && !parts[1].isBlank()) timeTo = parseTime(parts[1]);
                }
                default -> { }
            }
        }
        if (ownerOnly) builder.ownerOnly(true);
        if (!orgPaths.isEmpty()) builder.orgPaths(orgPaths);
        if (!businessLineIds.isEmpty()) builder.businessLineIds(businessLineIds);
        if (!regions.isEmpty()) builder.regions(regions);
        if (!tags.isEmpty()) builder.tags(tags);
        if (timeFrom != null) builder.timeFrom(timeFrom);
        if (timeTo != null) builder.timeTo(timeTo);
        return builder.build();
    }

    @Override
    public <T> T applyColumnMask(T entity, Long userId, String resourceType) {
        if (entity == null) {
            return entity;
        }
        // 脱敏必须在 DTO 层面进行，禁止直接修改受管 JPA 实体：若传入实体，
        // 同一事务后续读取会拿到脱敏后的值，造成数据污染。遇到实体直接跳过并告警。
        if (entity.getClass().isAnnotationPresent(Entity.class)) {
            log.warn("列级脱敏跳过 JPA 实体 {}，请改为在 toDTO 之后对 DTO 执行脱敏", entity.getClass().getName());
            return entity;
        }
        Long tenantId = TenantContext.getTenantId();
        List<ColumnMask> masks = columnMaskRepository.findByResourceTypeAndTenantId(resourceType, tenantId);
        if (masks.isEmpty()) {
            return entity;
        }
        Class<?> clazz = entity.getClass();
        for (ColumnMask mask : masks) {
            if (!"ACTIVE".equals(mask.getStatus())) {
                continue;
            }
            if (isUserAllowed(mask, userId)) {
                continue;
            }
            try {
                Field field = clazz.getDeclaredField(mask.getFieldName());
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value instanceof String s) {
                    field.set(entity, maskValue(s, mask.getMaskType(), mask.getMaskRule()));
                }
            } catch (Exception ignored) {
            }
        }
        return entity;
    }

    @Override
    public <T> List<T> applyColumnMask(List<T> entities, Long userId, String resourceType) {
        if (entities == null) {
            return entities;
        }
        for (T e : entities) {
            applyColumnMask(e, userId, resourceType);
        }
        return entities;
    }

    private boolean isUserAllowed(ColumnMask mask, Long userId) {
        if (userId == null || mask.getRoleIds() == null || mask.getRoleIds().isBlank()) {
            return false;
        }
        return parseRoleIds(mask.getRoleIds()).contains(userId);
    }

    private List<Long> parseRoleIds(String s) {
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

    private String maskValue(String value, String maskType, String maskRule) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if ("HIDE".equals(maskType)) {
            return "****";
        }
        if ("PARTIAL".equals(maskType) && maskRule != null && !maskRule.isBlank()) {
            return maskRule;
        }
        if ("MASK".equals(maskType)) {
            int len = value.length();
            if (len <= 2) {
                return "*".repeat(len);
            }
            return value.substring(0, 1) + "*".repeat(len - 2) + value.substring(len - 1);
        }
        return value;
    }

    private void parseLongs(String config, List<Long> target) {
        if (config == null) return;
        for (String s : config.split(",")) {
            try { target.add(Long.valueOf(s.trim())); } catch (Exception ignored) { }
        }
    }

    private void parseStrings(String config, List<String> target) {
        if (config == null) return;
        for (String s : config.split(",")) {
            if (!s.isBlank()) target.add(s.trim());
        }
    }

    private LocalDateTime parseTime(String s) {
        try {
            return LocalDateTime.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
