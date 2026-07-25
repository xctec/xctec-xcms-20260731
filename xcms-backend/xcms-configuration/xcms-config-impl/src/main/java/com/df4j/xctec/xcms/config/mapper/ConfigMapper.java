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

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConfigMapper {

    DictTypeDTO toDto(DictType entity);

    DictItemDTO toDto(DictItem entity);

    ConfigSwitchDTO toDto(ConfigSwitch entity);

    CodeRuleDTO toDto(CodeRule entity);

    List<DictItemDTO> toItemDtoList(List<DictItem> entities);

    List<DictTypeDTO> toTypeDtoList(List<DictType> entities);
}
