package com.df4j.xctec.xcms.org.api;

import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.kernel.common.PageQuery;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.org.api.dto.DepartmentCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.DepartmentDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentTreeDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentUpdateRequest;

import java.util.List;

/**
 * 组织（部门）服务
 */
public interface OrganizationService {

    DepartmentDTO createDepartment(DepartmentCreateRequest request);

    DepartmentDTO updateDepartment(Long deptId, DepartmentUpdateRequest request);

    void deleteDepartment(Long deptId);

    DepartmentDTO getDepartment(Long deptId);

    List<DepartmentTreeDTO> getDepartmentTree();

    List<DepartmentDTO> listSubDepartments(Long parentId);

    void moveDepartment(Long deptId, Long newParentId);

    PageResult<UserBriefDTO> listDepartmentUsers(Long deptId, PageQuery query);
}
