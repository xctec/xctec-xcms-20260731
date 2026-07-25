package com.df4j.xctec.xcms.identity.mapper;

import com.df4j.xctec.xcms.identity.api.dto.RoleDTO;
import com.df4j.xctec.xcms.identity.api.dto.RoleUpdateRequest;
import com.df4j.xctec.xcms.identity.domain.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {

    RoleDTO toDTO(Role role);

    List<RoleDTO> toDTOList(List<Role> roles);

    void updateRole(@MappingTarget Role role, RoleUpdateRequest request);
}
