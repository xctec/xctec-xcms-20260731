package com.df4j.xctec.xcms.identity.mapper;

import com.df4j.xctec.xcms.identity.api.dto.UserDTO;
import com.df4j.xctec.xcms.identity.api.dto.UserUpdateRequest;
import com.df4j.xctec.xcms.identity.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserDTO toDTO(User user);

    List<UserDTO> toDTOList(List<User> users);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
