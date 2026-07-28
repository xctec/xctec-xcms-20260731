package com.df4j.xctec.xcms.datapermission.mask;

import com.df4j.xctec.xcms.datapermission.api.ColumnMaskRuleProvider;
import com.df4j.xctec.xcms.datapermission.api.ColumnMaskSpec;
import com.df4j.xctec.xcms.datapermission.api.MaskField;
import com.df4j.xctec.xcms.kernel.context.ActorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DTO 层列级脱敏处理器（ADR-013 / AT-18）。
 *
 * <p>注解驱动：DTO 字段以 {@link MaskField} 声明敏感列，在 toDTO 之后执行脱敏。
 * 相比旧的 ColumnMaskResponseBodyAdvice（S13）：</p>
 * <ul>
 *   <li>fail-closed：租户上下文缺失或规则缺失时默认脱敏（"****"），不裸奔；</li>
 *   <li>与 ApiResponse 解耦：直接作用于任意 DTO 对象/集合；</li>
 *   <li>service principal（服务间调用）不脱敏，用户出网才脱敏。</li>
 * </ul>
 */
@Slf4j
@Component
public class MaskFieldPostProcessor {

    private static final String FAIL_CLOSED_MASK = "****";

    /** DTO 类 → @MaskField 字段元数据缓存（无标注的类缓存空列表，快速跳过） */
    private final Map<Class<?>, List<MaskedFieldMeta>> metaCache = new ConcurrentHashMap<>();

    private final ObjectProvider<ColumnMaskRuleProvider> maskProviders;

    public MaskFieldPostProcessor(ObjectProvider<ColumnMaskRuleProvider> maskProviders) {
        this.maskProviders = maskProviders;
    }

    /** 对单个 DTO 或集合执行脱敏（就地修改并返回原对象） */
    public Object mask(Object dto) {
        if (dto == null) {
            return null;
        }
        if (dto instanceof Collection<?> collection) {
            for (Object item : collection) {
                maskSingle(item);
            }
            return dto;
        }
        maskSingle(dto);
        return dto;
    }

    /** 判断类型（或集合元素类型）是否含 @MaskField 字段 */
    public boolean hasMaskFields(Object dto) {
        if (dto == null) {
            return false;
        }
        if (dto instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (item != null) {
                    return !metaFor(item.getClass()).isEmpty();
                }
            }
            return false;
        }
        return !metaFor(dto.getClass()).isEmpty();
    }

    private void maskSingle(Object dto) {
        if (dto == null) {
            return;
        }
        List<MaskedFieldMeta> metas = metaFor(dto.getClass());
        if (metas.isEmpty()) {
            return;
        }
        // 服务间调用（service principal）不脱敏：脱敏只针对用户出网数据（AT-18）
        ActorContext.Principal principal = ActorContext.getPrincipal();
        if (principal != null && principal.isService()) {
            return;
        }
        Long userId = ActorContext.getCurrentUserId();
        Long tenantId = ActorContext.getTenantId();
        for (MaskedFieldMeta meta : metas) {
            try {
                Object value = meta.field().get(dto);
                if (!(value instanceof String s) || s.isEmpty()) {
                    continue;
                }
                meta.field().set(dto, resolveMasked(s, meta, tenantId, userId));
            } catch (Exception e) {
                log.warn("列级脱敏字段 {}.{} 处理失败: {}", dto.getClass().getSimpleName(),
                        meta.field().getName(), e.getMessage());
            }
        }
    }

    private String resolveMasked(String value, MaskedFieldMeta meta, Long tenantId, Long userId) {
        // fail-closed：租户上下文缺失 → 默认脱敏
        if (tenantId == null) {
            return FAIL_CLOSED_MASK;
        }
        ColumnMaskSpec spec = findSpec(meta, tenantId);
        // fail-closed：规则缺失 → 默认脱敏（修复旧链路 fail-open 裸奔问题）
        if (spec == null) {
            return FAIL_CLOSED_MASK;
        }
        if (userId != null && spec.getExemptIds() != null && spec.getExemptIds().contains(userId)) {
            return value;
        }
        return maskValue(value, spec.getMaskType(), spec.getMaskRule());
    }

    private ColumnMaskSpec findSpec(MaskedFieldMeta meta, Long tenantId) {
        List<ColumnMaskSpec> masks = new ArrayList<>();
        maskProviders.forEach(p -> {
            List<ColumnMaskSpec> provided = p.getMasks(meta.resourceType(), tenantId);
            if (provided != null) {
                masks.addAll(provided);
            }
        });
        return masks.stream()
                .filter(m -> meta.ruleField().equals(m.getFieldName()))
                .findFirst()
                .orElse(null);
    }

    private String maskValue(String value, String maskType, String maskRule) {
        if ("HIDE".equals(maskType)) {
            return FAIL_CLOSED_MASK;
        }
        if ("PARTIAL".equals(maskType) && maskRule != null && !maskRule.isBlank()) {
            return maskRule;
        }
        if ("MASK".equals(maskType)) {
            int len = value.length();
            if (len <= 2) {
                return "*".repeat(len);
            }
            return value.charAt(0) + "*".repeat(len - 2) + value.substring(len - 1);
        }
        // 未知类型 fail-closed
        return FAIL_CLOSED_MASK;
    }

    private List<MaskedFieldMeta> metaFor(Class<?> clazz) {
        return metaCache.computeIfAbsent(clazz, c -> {
            List<MaskedFieldMeta> metas = new ArrayList<>();
            for (Class<?> cur = c; cur != null && cur != Object.class; cur = cur.getSuperclass()) {
                for (Field field : cur.getDeclaredFields()) {
                    MaskField annotation = field.getAnnotation(MaskField.class);
                    if (annotation != null) {
                        field.setAccessible(true);
                        String ruleField = annotation.field().isBlank() ? field.getName() : annotation.field();
                        metas.add(new MaskedFieldMeta(field, annotation.resourceType(), ruleField));
                    }
                }
            }
            return List.copyOf(metas);
        });
    }

    private record MaskedFieldMeta(Field field, String resourceType, String ruleField) {
    }
}
