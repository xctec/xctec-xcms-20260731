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
import com.df4j.xctec.xcms.config.domain.CodeRule;
import com.df4j.xctec.xcms.config.domain.ConfigSwitch;
import com.df4j.xctec.xcms.config.domain.DictItem;
import com.df4j.xctec.xcms.config.domain.DictType;
import com.df4j.xctec.xcms.config.mapper.ConfigMapper;
import com.df4j.xctec.xcms.config.repository.CodeRuleRepository;
import com.df4j.xctec.xcms.config.repository.ConfigSwitchRepository;
import com.df4j.xctec.xcms.config.repository.DictItemRepository;
import com.df4j.xctec.xcms.config.repository.DictTypeRepository;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final DictTypeRepository dictTypeRepository;
    private final DictItemRepository dictItemRepository;
    private final ConfigSwitchRepository configSwitchRepository;
    private final CodeRuleRepository codeRuleRepository;
    private final ConfigMapper configMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DictItemDTO> getDictItems(String typeCode) {
        return dictItemRepository.findByTypeCode(typeCode).stream()
                .sorted(Comparator.comparing(DictItem::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(configMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DictItemDTO getDictItem(String typeCode, String itemCode) {
        return dictItemRepository.findByTypeCodeAndItemCode(typeCode, itemCode)
                .map(configMapper::toDto)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "字典项不存在: " + typeCode + "/" + itemCode));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSwitchOn(String switchKey) {
        return configSwitchRepository.findBySwitchKey(switchKey).map(ConfigSwitch::isEnabled).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String key) {
        return configSwitchRepository.findBySwitchKey(key).map(ConfigSwitch::getSwitchValue).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public long getLongValue(String key, long defaultValue) {
        return configSwitchRepository.findBySwitchKey(key)
                .map(ConfigSwitch::getSwitchValue)
                .filter(v -> v != null && !v.isBlank())
                .map(v -> {
                    try {
                        return Long.parseLong(v.trim());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    @Override
    @Transactional
    public String nextCode(String ruleCode) {
        CodeRule rule = codeRuleRepository.findByRuleCodeForUpdate(ruleCode)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "编码规则不存在: " + ruleCode));
        rule.setCurrentVal(rule.getCurrentVal() + rule.getStep());
        String code = rule.getPrefix() + String.format("%0" + rule.getSeqLength() + "d", rule.getCurrentVal());
        rule.setExample(code);
        codeRuleRepository.save(rule);
        return code;
    }

    @Override
    @Transactional
    public DictTypeDTO createDictType(DictTypeCreateRequest request) {
        if (dictTypeRepository.findByTypeCode(request.getTypeCode()).isPresent()) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, "字典类型已存在: " + request.getTypeCode());
        }
        DictType type = new DictType();
        type.setTypeCode(request.getTypeCode());
        type.setTypeName(request.getTypeName());
        type.setRemark(request.getRemark());
        type.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
        return configMapper.toDto(dictTypeRepository.save(type));
    }

    @Override
    @Transactional
    public DictItemDTO createDictItem(DictItemCreateRequest request) {
        if (dictTypeRepository.findByTypeCode(request.getTypeCode()).isEmpty()) {
            throw new BusinessException(ErrorCodes.NOT_FOUND, "字典类型不存在: " + request.getTypeCode());
        }
        if (dictItemRepository.findByTypeCodeAndItemCode(request.getTypeCode(), request.getItemCode()).isPresent()) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, "字典项已存在: " + request.getItemCode());
        }
        DictItem item = new DictItem();
        item.setTypeCode(request.getTypeCode());
        item.setItemCode(request.getItemCode());
        item.setItemName(request.getItemName());
        item.setItemValue(request.getItemValue());
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        item.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
        return configMapper.toDto(dictItemRepository.save(item));
    }

    @Override
    @Transactional
    public ConfigSwitchDTO createSwitch(ConfigSwitchCreateRequest request) {
        Optional<ConfigSwitch> exist = configSwitchRepository.findBySwitchKey(request.getSwitchKey());
        ConfigSwitch sw = exist.orElseGet(ConfigSwitch::new);
        sw.setSwitchKey(request.getSwitchKey());
        sw.setSwitchName(request.getSwitchName());
        sw.setEnabled(request.isEnabled());
        sw.setSwitchValue(request.getSwitchValue());
        sw.setRemark(request.getRemark());
        return configMapper.toDto(configSwitchRepository.save(sw));
    }

    @Override
    @Transactional
    public CodeRuleDTO createCodeRule(CodeRuleCreateRequest request) {
        if (codeRuleRepository.findByRuleCode(request.getRuleCode()).isPresent()) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, "编码规则已存在: " + request.getRuleCode());
        }
        CodeRule rule = new CodeRule();
        rule.setRuleCode(request.getRuleCode());
        rule.setRuleName(request.getRuleName());
        rule.setPrefix(request.getPrefix() == null ? "" : request.getPrefix());
        rule.setSeqLength(request.getSeqLength() <= 0 ? 6 : request.getSeqLength());
        rule.setStep(request.getStep() <= 0 ? 1 : request.getStep());
        rule.setCurrentVal(0);
        return configMapper.toDto(codeRuleRepository.save(rule));
    }

    @Override
    @Transactional
    public void setSwitch(String switchKey, boolean enabled) {
        ConfigSwitch sw = configSwitchRepository.findBySwitchKey(switchKey)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "配置开关不存在: " + switchKey));
        sw.setEnabled(enabled);
        configSwitchRepository.save(sw);
    }
}
