package com.df4j.xctec.xcms.org.mapper;

import com.df4j.xctec.xcms.org.api.dto.UserGroupDTO;
import com.df4j.xctec.xcms.org.domain.UserGroup;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserGroupMapper {

    UserGroupDTO toDTO(UserGroup group);

    List<UserGroupDTO> toDTOList(List<UserGroup> groups);
}
