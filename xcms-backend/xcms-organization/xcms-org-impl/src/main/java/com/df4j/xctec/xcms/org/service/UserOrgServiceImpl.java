package com.df4j.xctec.xcms.org.service;

import com.df4j.xctec.xcms.org.api.UserOrgService;
import com.df4j.xctec.xcms.org.api.dto.DepartmentDTO;
import com.df4j.xctec.xcms.org.api.dto.UserPositionDTO;
import com.df4j.xctec.xcms.org.domain.Department;
import com.df4j.xctec.xcms.org.domain.Position;
import com.df4j.xctec.xcms.org.domain.UserPosition;
import com.df4j.xctec.xcms.org.repository.DepartmentRepository;
import com.df4j.xctec.xcms.org.repository.PositionRepository;
import com.df4j.xctec.xcms.org.repository.UserPositionRepository;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserOrgServiceImpl implements UserOrgService {

    private final UserPositionRepository userPositionRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    @Override
    @Transactional
    public void assignUserToPosition(Long userId, Long deptId, Long positionId, boolean isPrimary) {
        requireTenant();
        if (isPrimary) {
            userPositionRepository.findByUserIdAndIsPrimaryTrue(userId)
                    .forEach(up -> {
                        up.setIsPrimary(false);
                        userPositionRepository.save(up);
                    });
        }
        UserPosition up = UserPosition.builder()
                .userId(userId).deptId(deptId).positionId(positionId)
                .isPrimary(isPrimary).createdBy(TenantContext.getCurrentUserId()).build();
        userPositionRepository.save(up);
    }

    @Override
    @Transactional
    public void removeUserFromPosition(Long userId, Long positionId) {
        requireTenant();
        // 软删除，与部门/用户组的删除策略保持一致（评审 P1.3）
        userPositionRepository.findByUserIdAndPositionId(userId, positionId).ifPresent(up -> {
            up.setDeletedAt(LocalDateTime.now());
            userPositionRepository.save(up);
        });
    }

    @Override
    public List<UserPositionDTO> getUserPositions(Long userId) {
        requireTenant();
        Map<Long, Department> deptMap = new LinkedHashMap<>();
        Map<Long, Position> posMap = new LinkedHashMap<>();
        List<UserPositionDTO> result = new ArrayList<>();
        for (UserPosition up : userPositionRepository.findByUserId(userId)) {
            UserPositionDTO dto = new UserPositionDTO();
            dto.setId(up.getId());
            dto.setUserId(up.getUserId());
            dto.setDeptId(up.getDeptId());
            dto.setPositionId(up.getPositionId());
            dto.setIsPrimary(up.getIsPrimary());
            Department dept = deptMap.computeIfAbsent(up.getDeptId(),
                    id -> departmentRepository.findByIdAndDeletedAtIsNull(id).orElse(null));
            dto.setDeptName(dept != null ? dept.getDeptName() : null);
            if (up.getPositionId() != null) {
                Position pos = posMap.computeIfAbsent(up.getPositionId(), id ->
                        positionRepository.findByIdAndDeletedAtIsNull(id).orElse(null));
                dto.setPositionName(pos != null ? pos.getPositionName() : null);
            }
            result.add(dto);
        }
        return result;
    }

    @Override
    public DepartmentDTO getUserPrimaryDept(Long userId) {
        requireTenant();
        UserPosition up = userPositionRepository.findByUserIdAndIsPrimaryTrue(userId).stream().findFirst()
                .orElseGet(() -> userPositionRepository.findByUserId(userId).stream().findFirst().orElse(null));
        if (up == null) {
            return null;
        }
        Department dept = departmentRepository.findByIdAndDeletedAtIsNull(up.getDeptId()).orElse(null);
        if (dept == null) {
            return null;
        }
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(dept.getId());
        dto.setDeptName(dept.getDeptName());
        dto.setDeptCode(dept.getDeptCode());
        dto.setParentId(dept.getParentId());
        dto.setLevel(dept.getLevel());
        dto.setPath(dept.getPath());
        dto.setManagerId(dept.getManagerId());
        dto.setSortOrder(dept.getSortOrder());
        dto.setStatus(dept.getStatus());
        return dto;
    }

    @Override
    public List<String> getUserDeptPaths(Long userId) {
        requireTenant();
        List<String> paths = new ArrayList<>();
        for (UserPosition up : userPositionRepository.findByUserId(userId)) {
            Department dept = departmentRepository.findByIdAndDeletedAtIsNull(up.getDeptId()).orElse(null);
            if (dept == null) {
                continue;
            }
            paths.add(buildNamePath(dept));
        }
        return paths;
    }

    private String buildNamePath(Department leaf) {
        List<String> names = new ArrayList<>();
        Department current = leaf;
        while (current != null) {
            names.add(0, current.getDeptName());
            if (current.getParentId() == null) {
                break;
            }
            current = departmentRepository.findByIdAndDeletedAtIsNull(current.getParentId()).orElse(null);
        }
        return String.join(" / ", names);
    }

    private void requireTenant() {
        if (TenantContext.getTenantId() == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "未确定租户上下文");
        }
    }
}
