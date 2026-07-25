package com.df4j.xctec.xcms.org.mapper;

import com.df4j.xctec.xcms.identity.api.UserService;
import com.df4j.xctec.xcms.identity.api.dto.UserDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentTreeDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentUpdateRequest;
import com.df4j.xctec.xcms.org.domain.Department;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DepartmentMapper {

    @Autowired
    protected UserService userService;

    @Mapping(target = "managerName", ignore = true)
    public abstract DepartmentDTO toDTO(Department department);

    public abstract List<DepartmentDTO> toDTOList(List<Department> departments);

    @Mapping(target = "children", ignore = true)
    public abstract DepartmentTreeDTO toTreeDTO(Department department);

    public abstract void updateDepartment(@MappingTarget Department department, DepartmentUpdateRequest request);

    @AfterMapping
    void fillManagerName(Department department, @MappingTarget DepartmentDTO dto) {
        if (department.getManagerId() != null && dto.getManagerName() == null) {
            try {
                UserDTO user = userService.getUserById(department.getManagerId());
                dto.setManagerName(user.getRealName());
            } catch (Exception ignored) {
            }
        }
    }
}
