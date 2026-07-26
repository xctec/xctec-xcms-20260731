package com.df4j.xctec.xcms.config.api;

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

import java.util.List;

/**
 * 配置中心服务接口。
 */
public interface ConfigService {

    /** 获取字典项列表（按字典编码） */
    List<DictItemDTO> getDictItems(String dictCode);

    /** 获取单个字典项 */
    DictItemDTO getDictItem(String dictCode, String itemCode);

    /** 功能开关是否开启 */
    boolean isSwitchOn(String featureCode);

    /** 读取配置值（字符串） */
    String getConfigValue(String key);

    /** 读取长整型配置值，缺失或解析失败时返回默认值 */
    long getLongValue(String key, long defaultValue);

    /** 生成下一个业务编码 */
    String nextCode(NextCodeRequest request);

    DictTypeDTO createDictType(DictTypeCreateRequest request);

    DictItemDTO createDictItem(DictItemCreateRequest request);

    ConfigSwitchDTO createConfigSwitch(ConfigSwitchCreateRequest request);

    CodeRuleDTO createCodeRule(CodeRuleCreateRequest request);

    void setSwitch(SetSwitchRequest request);
}
