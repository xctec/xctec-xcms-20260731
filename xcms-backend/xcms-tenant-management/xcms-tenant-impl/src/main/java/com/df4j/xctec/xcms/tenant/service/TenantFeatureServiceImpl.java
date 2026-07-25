package com.df4j.xctec.xcms.tenant.service;

import com.df4j.xctec.xcms.tenant.api.TenantFeatureService;
import com.df4j.xctec.xcms.tenant.api.dto.TenantFeatureDTO;
import com.df4j.xctec.xcms.tenant.domain.TenantFeature;
import com.df4j.xctec.xcms.tenant.mapper.TenantMapper;
import com.df4j.xctec.xcms.tenant.repository.TenantFeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 租户功能开关实现
 */
@Service
@RequiredArgsConstructor
public class TenantFeatureServiceImpl implements TenantFeatureService {

    private final TenantFeatureRepository tenantFeatureRepository;
    private final TenantMapper tenantMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TenantFeatureDTO> getFeatures(Long tenantId) {
        return tenantMapper.toFeatureDtoList(tenantFeatureRepository.findByTenantId(tenantId));
    }

    @Override
    @Transactional
    public void toggleFeature(Long tenantId, String featureCode, boolean enabled) {
        TenantFeature feature = tenantFeatureRepository
                .findByTenantIdAndFeatureCode(tenantId, featureCode)
                .orElseGet(() -> {
                    TenantFeature f = new TenantFeature();
                    f.setTenantId(tenantId);
                    f.setFeatureCode(featureCode);
                    f.setEnabled(true);
                    return f;
                });
        feature.setEnabled(enabled);
        tenantFeatureRepository.save(feature);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(Long tenantId, String featureCode) {
        return tenantFeatureRepository
                .findByTenantIdAndFeatureCode(tenantId, featureCode)
                .map(TenantFeature::getEnabled)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public String getFeatureConfig(Long tenantId, String featureCode) {
        return tenantFeatureRepository
                .findByTenantIdAndFeatureCode(tenantId, featureCode)
                .map(TenantFeature::getConfig)
                .orElse(null);
    }
}
