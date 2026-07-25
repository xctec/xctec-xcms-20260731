package com.df4j.xctec.xcms.tenant.mapper;

import com.df4j.xctec.xcms.tenant.api.dto.QuotaUsageItem;
import com.df4j.xctec.xcms.tenant.api.dto.TenantDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantFeatureDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuotaDTO;
import com.df4j.xctec.xcms.tenant.domain.TenantFeature;
import com.df4j.xctec.xcms.tenant.domain.TenantInfo;
import com.df4j.xctec.xcms.tenant.domain.TenantQuota;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

/**
 * 租户实体与 DTO 映射
 */
@Mapper(componentModel = "spring")
public interface TenantMapper {

    TenantDTO toDto(TenantInfo entity);

    List<TenantDTO> toDtoList(List<TenantInfo> entities);

    TenantQuotaDTO toDto(TenantQuota entity);

    List<TenantQuotaDTO> toQuotaDtoList(List<TenantQuota> entities);

    TenantFeatureDTO toDto(TenantFeature entity);

    List<TenantFeatureDTO> toFeatureDtoList(List<TenantFeature> entities);

    /**
     * 配额实体转使用明细（计算使用百分比）
     */
    @Named("toUsageItem")
    default QuotaUsageItem toUsageItem(TenantQuota quota) {
        QuotaUsageItem item = new QuotaUsageItem();
        item.setQuotaType(quota.getQuotaType());
        item.setLimit(quota.getQuotaLimit());
        item.setUsed(quota.getQuotaUsed());
        double percentage = (quota.getQuotaLimit() == null || quota.getQuotaLimit() <= 0)
                ? 0d
                : (quota.getQuotaUsed().doubleValue() / quota.getQuotaLimit()) * 100d;
        item.setUsagePercentage(Math.round(percentage * 100d) / 100d);
        return item;
    }
}
