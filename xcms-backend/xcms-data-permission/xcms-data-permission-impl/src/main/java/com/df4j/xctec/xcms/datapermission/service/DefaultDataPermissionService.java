package com.df4j.xctec.xcms.datapermission.service;

import com.df4j.xctec.xcms.datapermission.api.ColumnMaskRuleProvider;
import com.df4j.xctec.xcms.datapermission.api.ColumnMaskSpec;
import com.df4j.xctec.xcms.datapermission.api.DataPermissionContext;
import com.df4j.xctec.xcms.datapermission.api.DataPermissionRuleProvider;
import com.df4j.xctec.xcms.datapermission.api.DataPermissionService;
import com.df4j.xctec.xcms.datapermission.api.DataRuleSpec;
import com.df4j.xctec.xcms.datapermission.cache.CachePort;
import com.df4j.xctec.xcms.kernel.context.ActorContext;
import jakarta.persistence.Entity;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限默认实现（ADR-013 / AT-16）：自 auth-impl 的 DataPermissionServiceImpl 迁入。
 *
 * <p>与原实现的差异：规则来源不再直连 perm_data_rule / perm_column_mask 仓储，
 * 而是聚合所有 {@link DataPermissionRuleProvider} / {@link ColumnMaskRuleProvider}
 * SPI 的返回（auth-impl 作为管理面提供者之一）；解析后的上下文经
 * {@link CachePort} 短 TTL 缓存。</p>
 */
@Slf4j
@Service
public class DefaultDataPermissionService implements DataPermissionService {

    private static final String CTX_KEY_PREFIX = "dp:ctx:";

    private final ObjectProvider<DataPermissionRuleProvider> ruleProviders;
    private final ObjectProvider<ColumnMaskRuleProvider> maskProviders;
    private final CachePort cachePort;

    public DefaultDataPermissionService(ObjectProvider<DataPermissionRuleProvider> ruleProviders,
                                        ObjectProvider<ColumnMaskRuleProvider> maskProviders,
                                        CachePort cachePort) {
        this.ruleProviders = ruleProviders;
        this.maskProviders = maskProviders;
        this.cachePort = cachePort;
    }

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
        Long tenantId = ActorContext.getTenantId();
        String cacheKey = CTX_KEY_PREFIX + tenantId + ":" + userId + ":" + resourceType;
        DataPermissionContext cached = cachePort.get(cacheKey, DataPermissionContext.class);
        if (cached != null) {
            return cached;
        }
        List<DataRuleSpec> rules = new ArrayList<>();
        ruleProviders.forEach(provider -> {
            List<DataRuleSpec> provided = provider.getRules(resourceType, tenantId, userId);
            if (provided != null) {
                rules.addAll(provided);
            }
        });
        DataPermissionContext ctx = buildContext(userId, tenantId, resourceType, rules);
        cachePort.put(cacheKey, ctx);
        return ctx;
    }

    private DataPermissionContext buildContext(Long userId, Long tenantId, String resourceType,
                                               List<DataRuleSpec> rules) {
        DataPermissionContext.Builder builder = DataPermissionContext.builder()
                .userId(userId).tenantId(tenantId).resourceType(resourceType);
        if (rules.isEmpty()) {
            return builder.build();
        }
        List<String> orgPaths = new ArrayList<>();
        List<Long> businessLineIds = new ArrayList<>();
        List<String> regions = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        LocalDateTime timeFrom = null;
        LocalDateTime timeTo = null;
        boolean ownerOnly = false;
        for (DataRuleSpec rule : rules) {
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
        Long tenantId = ActorContext.getTenantId();
        List<ColumnMaskSpec> masks = new ArrayList<>();
        maskProviders.forEach(provider -> {
            List<ColumnMaskSpec> provided = provider.getMasks(resourceType, tenantId);
            if (provided != null) {
                masks.addAll(provided);
            }
        });
        if (masks.isEmpty()) {
            return entity;
        }
        Class<?> clazz = entity.getClass();
        for (ColumnMaskSpec mask : masks) {
            if (isExempt(mask, userId)) {
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

    @Override
    public void evictContextCache(Long tenantId) {
        cachePort.evictByPrefix(CTX_KEY_PREFIX + tenantId + ":");
    }

    private boolean isExempt(ColumnMaskSpec mask, Long userId) {
        return userId != null && mask.getExemptIds() != null && mask.getExemptIds().contains(userId);
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
