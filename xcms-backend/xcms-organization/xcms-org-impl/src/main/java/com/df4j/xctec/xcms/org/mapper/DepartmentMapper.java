package com.df4j.xctec.xcms.org.mapper;

import com.df4j.xctec.xcms.org.api.dto.DepartmentDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentTreeDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentUpdateRequest;
import com.df4j.xctec.xcms.org.domain.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DepartmentMapper {

    @Mapping(target = "managerName", ignore = true)
    DepartmentDTO toDTO(Department department);

    List<DepartmentDTO> toDTOList(List<Department> departments);

    @Mapping(target = "children", ignore = true)
    DepartmentTreeDTO toTreeDTO(Department department);

    void updateDepartment(@MappingTarget Department department, DepartmentUpdateRequest request);
}
