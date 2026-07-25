package com.df4j.xctec.xcms.config.api;

import com.df4j.xctec.xcms.config.api.dto.CodeRuleDTO;
import com.df4j.xctec.xcms.config.api.dto.ConfigSwitchDTO;
import com.df4j.xctec.xcms.config.api.dto.DictItemDTO;
import com.df4j.xctec.xcms.config.api.dto.DictTypeDTO;
import com.df4j.xctec.xcms.config.api.dto.request.CodeRuleCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.ConfigSwitchCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.DictItemCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.DictTypeCreateRequest;

import java.util.List;

/**
 * 配置中心服务：字典、开关/通用配置值、编码规则。
 * 供其他模块引用（字典/开关/编码规则）。
 */
public interface ConfigService {

    /** 获取字典类型下的所有字典项（按 sortOrder 排序） */
    List<DictItemDTO> getDictItems(String typeCode);

    /** 获取单个字典项 */
    DictItemDTO getDictItem(String typeCode, String itemCode);

    /** 开关是否开启 */
    boolean isSwitchOn(String switchKey);

    /** 读取通用配置字符串值，不存在返回 null */
    String getConfigValue(String key);

    /** 读取通用配置长整值，不存在或无法解析时返回默认值 */
    long getLongValue(String key, long defaultValue);

    /** 生成下一个编码（原子递增，线程/事务安全） */
    String nextCode(String ruleCode);

    /** 管理面：创建字典类型 */
    DictTypeDTO createDictType(DictTypeCreateRequest request);

    /** 管理面：创建字典项 */
    DictItemDTO createDictItem(DictItemCreateRequest request);

    /** 管理面：创建配置开关 */
    ConfigSwitchDTO createSwitch(ConfigSwitchCreateRequest request);

    /** 管理面：创建编码规则 */
    CodeRuleDTO createCodeRule(CodeRuleCreateRequest request);

    /** 管理面：设置开关状态 */
    void setSwitch(String switchKey, boolean enabled);
}
