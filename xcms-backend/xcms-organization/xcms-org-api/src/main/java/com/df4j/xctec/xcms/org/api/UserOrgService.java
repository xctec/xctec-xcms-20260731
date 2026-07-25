package com.df4j.xctec.xcms.org.api;

import com.df4j.xctec.xcms.org.api.dto.DepartmentDTO;
import com.df4j.xctec.xcms.org.api.dto.UserPositionDTO;

import java.util.List;

/**
 * 组织关系服务（用户与部门/岗位关联）
 */
public interface UserOrgService {

    void assignUserToPosition(Long userId, Long deptId, Long positionId, boolean isPrimary);

    void removeUserFromPosition(Long userId, Long positionId);

    List<UserPositionDTO> getUserPositions(Long userId);

    DepartmentDTO getUserPrimaryDept(Long userId);

    List<String> getUserDeptPaths(Long userId);
}
