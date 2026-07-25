package com.df4j.xctec.xcms.org.mapper;

import com.df4j.xctec.xcms.org.api.dto.PositionDTO;
import com.df4j.xctec.xcms.org.api.dto.PositionUpdateRequest;
import com.df4j.xctec.xcms.org.domain.Position;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PositionMapper {

    PositionDTO toDTO(Position position);

    List<PositionDTO> toDTOList(List<Position> positions);

    void updatePosition(@MappingTarget Position position, PositionUpdateRequest request);
}
