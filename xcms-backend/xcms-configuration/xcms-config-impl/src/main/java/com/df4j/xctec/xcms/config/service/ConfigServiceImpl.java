package com.df4j.xctec.xcms.config.service;

import com.df4j.xctec.xcms.config.api.ConfigService;
import com.df4j.xctec.xcms.config.api.dto.CodeRuleDTO;
import com.df4j.xctec.xcms.config.api.dto.ConfigSwitchDTO;
import com.df4j.xctec.xcms.config.api.dto.DictItemDTO;
import com.df4j.xctec.xcms.config.api.dto.DictTypeDTO;
import com.df4j.xctec.xcms.config.api.dto.request.CodeRuleCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.ConfigSwitchCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.DictItemCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.DictTypeCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.NextCodeRequest;
import com.df4j.xctec.xcms.config.api.dto.request.SetSwitchRequest;
import com.df4j.xctec.xcms.config.domain.CodeRule;
import com.df4j.xctec.xcms.config.domain.ConfigParam;
import com.df4j.xctec.xcms.config.domain.ConfigSwitch;
import com.df4j.xctec.xcms.config.domain.DictItem;
import com.df4j.xctec.xcms.config.domain.DictType;
import com.df4j.xctec.xcms.config.mapper.ConfigMapper;
import com.df4j.xctec.xcms.config.repository.CodeRuleRepository;
import com.df4j.xctec.xcms.config.repository.ConfigParamRepository;
import com.df4j.xctec.xcms.config.repository.ConfigSwitchRepository;
import com.df4j.xctec.xcms.config.repository.DictItemRepository;
import com.df4j.xctec.xcms.config.repository.DictTypeRepository;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final DictTypeRepository dictTypeRepository;
    private final DictItemRepository dictItemRepository;
    private final ConfigSwitchRepository configSwitchRepository;
    private final CodeRuleRepository codeRuleRepository;
    private final ConfigParamRepository configParamRepository;
    private final ConfigMapper configMapper;

    @Override
    public List<DictItemDTO> getDictItems(String dictCode) {
        DictType dt = dictTypeRepository.findByDictCode(dictCode)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "字典不存在: " + dictCode));
        return configMapper.toDictItemDTOList(dictItemRepository.findByDictId(dt.getId()));
    }

    @Override
    public DictItemDTO getDictItem(String dictCode, String itemCode) {
        DictType dt = dictTypeRepository.findByDictCode(dictCode)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "字典不存在: " + dictCode));
        return dictItemRepository.findByDictIdAndItemCode(dt.getId(), itemCode)
                .map(configMapper::toDictItemDTO)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "字典项不存在: " + itemCode));
    }

    @Override
    public boolean isSwitchOn(String featureCode) {
        return configSwitchRepository.findByFeatureCode(featureCode)
                .map(ConfigSwitch::isEnabled)
                .orElse(false);
    }

    @Override
    public String getConfigValue(String key) {
        return configParamRepository.findByParamKey(key)
                .map(ConfigParam::getParamValue)
                .orElse(null);
    }

    @Override
    public long getLongValue(String key, long defaultValue) {
        ConfigParam p = configParamRepository.findByParamKey(key).orElse(null);
        if (p == null || p.getParamValue() == null || p.getParamValue().isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(p.getParamValue().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    @Transactional
    public String nextCode(NextCodeRequest request) {
        CodeRule rule = codeRuleRepository.findByRuleCodeForUpdate(request.getRuleCode())
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "编码规则不存在: " + request.getRuleCode()));
        rule.setCurrentSeq(rule.getCurrentSeq() + 1);
        codeRuleRepository.save(rule);
        String prefix = rule.getPrefix() == null ? "" : rule.getPrefix();
        return prefix + String.format("%0" + rule.getSeqLength() + "d", rule.getCurrentSeq());
    }

    @Override
    @Transactional
    public DictTypeDTO createDictType(DictTypeCreateRequest request) {
        if (dictTypeRepository.findByDictCode(request.getDictCode()).isPresent()) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, "字典编码已存在: " + request.getDictCode());
        }
        DictType entity = new DictType();
        entity.setDictCode(request.getDictCode());
        entity.setDictName(request.getDictName());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());
        return configMapper.toDictTypeDTO(dictTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public DictItemDTO createDictItem(DictItemCreateRequest request) {
        DictType dt = dictTypeRepository.findByDictCode(request.getDictCode())
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "字典不存在: " + request.getDictCode()));
        DictItem entity = new DictItem();
        entity.setDictId(dt.getId());
        entity.setItemCode(request.getItemCode());
        entity.setItemValue(request.getItemValue());
        entity.setSortOrder(request.getSortOrder());
        entity.setParentId(request.getParentId());
        entity.setStatus(request.getStatus());
        return configMapper.toDictItemDTO(dictItemRepository.save(entity));
    }

    @Override
    @Transactional
    public ConfigSwitchDTO createConfigSwitch(ConfigSwitchCreateRequest request) {
        if (configSwitchRepository.findByFeatureCode(request.getFeatureCode()).isPresent()) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, "功能开关已存在: " + request.getFeatureCode());
        }
        ConfigSwitch entity = new ConfigSwitch();
        entity.setFeatureCode(request.getFeatureCode());
        entity.setDescription(request.getDescription());
        entity.setEnabled(request.isEnabled());
        entity.setConfig(request.getConfig());
        return configMapper.toConfigSwitchDTO(configSwitchRepository.save(entity));
    }

    @Override
    @Transactional
    public CodeRuleDTO createCodeRule(CodeRuleCreateRequest request) {
        if (codeRuleRepository.findByRuleCode(request.getRuleCode()).isPresent()) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, "编码规则已存在: " + request.getRuleCode());
        }
        CodeRule entity = new CodeRule();
        entity.setRuleCode(request.getRuleCode());
        entity.setRuleName(request.getRuleName());
        entity.setPrefix(request.getPrefix());
        entity.setSeqLength(request.getSeqLength());
        entity.setCurrentSeq(0L);
        entity.setPattern(request.getPattern());
        entity.setResetCycle(request.getResetCycle());
        return configMapper.toCodeRuleDTO(codeRuleRepository.save(entity));
    }

    @Override
    @Transactional
    public void setSwitch(SetSwitchRequest request) {
        ConfigSwitch s = configSwitchRepository.findByFeatureCode(request.getFeatureCode())
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "功能开关不存在: " + request.getFeatureCode()));
        s.setEnabled(request.isEnabled());
        configSwitchRepository.save(s);
    }
}
