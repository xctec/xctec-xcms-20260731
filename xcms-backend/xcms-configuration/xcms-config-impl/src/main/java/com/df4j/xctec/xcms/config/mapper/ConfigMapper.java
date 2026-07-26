package com.df4j.xctec.xcms.config.mapper;

import com.df4j.xctec.xcms.config.api.dto.CodeRuleDTO;
import com.df4j.xctec.xcms.config.api.dto.ConfigSwitchDTO;
import com.df4j.xctec.xcms.config.api.dto.DictItemDTO;
import com.df4j.xctec.xcms.config.api.dto.DictTypeDTO;
import com.df4j.xctec.xcms.config.domain.CodeRule;
import com.df4j.xctec.xcms.config.domain.ConfigSwitch;
import com.df4j.xctec.xcms.config.domain.DictItem;
import com.df4j.xctec.xcms.config.domain.DictType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConfigMapper {

    DictTypeDTO toDictTypeDTO(DictType entity);

    DictItemDTO toDictItemDTO(DictItem entity);

    ConfigSwitchDTO toConfigSwitchDTO(ConfigSwitch entity);

    @Mapping(target = "example", ignore = true)
    CodeRuleDTO toCodeRuleDTO(CodeRule entity);

    List<DictItemDTO> toDictItemDTOList(List<DictItem> entities);
}
