package com.df4j.xctec.xcms.org.service;

import com.df4j.xctec.xcms.identity.api.UserService;
import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.org.api.OrganizationService;
import com.df4j.xctec.xcms.org.api.dto.DepartmentCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.DepartmentDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentTreeDTO;
import com.df4j.xctec.xcms.org.api.dto.DepartmentUpdateRequest;
import com.df4j.xctec.xcms.org.api.event.DepartmentCreatedEvent;
import com.df4j.xctec.xcms.org.api.event.DepartmentMovedEvent;
import com.df4j.xctec.xcms.org.domain.Department;
import com.df4j.xctec.xcms.org.mapper.DepartmentMapper;
import com.df4j.xctec.xcms.org.repository.DepartmentRepository;
import com.df4j.xctec.xcms.org.repository.UserPositionRepository;
import com.df4j.xctec.xcms.kernel.common.PageQuery;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final UserPositionRepository userPositionRepository;
    private final UserService userService;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public DepartmentDTO createDepartment(DepartmentCreateRequest request) {
        requireTenant();
        if (departmentRepository.existsByDeptCodeAndDeletedAtIsNull(request.getDeptCode())) {
            throw new BusinessException(ErrorCodes.DEPT_ALREADY_EXISTS, request.getDeptCode());
        }
        Department dept = new Department();
        dept.setDeptCode(request.getDeptCode());
        dept.setDeptName(request.getDeptName());
        dept.setParentId(request.getParentId());
        dept.setManagerId(request.getManagerId());
        dept.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        dept.setStatus("ACTIVE");
        dept.setCreatedBy(TenantContext.getCurrentUserId());

        int level = 1;
        String parentPath = null;
        if (request.getParentId() != null) {
            Department parent = departmentRepository.findByIdAndDeletedAtIsNull(request.getParentId()).orElse(null);
            if (parent != null) {
                level = (parent.getLevel() == null ? 1 : parent.getLevel()) + 1;
                parentPath = parent.getPath();
            }
        }
        dept.setLevel(level);
        dept = departmentRepository.save(dept);
        dept.setPath(parentPath == null ? "/" + dept.getId() : parentPath + "/" + dept.getId());
        dept = departmentRepository.save(dept);

        DepartmentCreatedEvent deptEvent = new DepartmentCreatedEvent();
        deptEvent.setDeptId(dept.getId());
        deptEvent.setTenantId(TenantContext.getTenantId());
        deptEvent.setDeptCode(dept.getDeptCode());
        deptEvent.setDeptName(dept.getDeptName());
        deptEvent.setParentId(dept.getParentId());
        eventPublisher.publish(deptEvent);
        return departmentMapper.toDTO(dept);
    }

    @Override
    @Transactional
    public DepartmentDTO updateDepartment(Long deptId, DepartmentUpdateRequest request) {
        requireTenant();
        Department dept = findDept(deptId);
        departmentMapper.updateDepartment(dept, request);
        dept.setUpdatedBy(TenantContext.getCurrentUserId());
        return departmentMapper.toDTO(departmentRepository.save(dept));
    }

    @Override
    @Transactional
    public void deleteDepartment(Long deptId) {
        requireTenant();
        Department dept = findDept(deptId);
        dept.setDeletedAt(java.time.LocalDateTime.now());
        dept.setUpdatedBy(TenantContext.getCurrentUserId());
        departmentRepository.save(dept);
    }

    @Override
    public DepartmentDTO getDepartment(Long deptId) {
        requireTenant();
        return departmentMapper.toDTO(findDept(deptId));
    }

    @Override
    public List<DepartmentTreeDTO> getDepartmentTree() {
        requireTenant();
        List<Department> all = departmentRepository.findByDeletedAtIsNullOrderBySortOrderAsc();
        Map<Long, DepartmentTreeDTO> nodeMap = new LinkedHashMap<>();
        Map<Long, List<DepartmentTreeDTO>> childrenMap = new LinkedHashMap<>();
        for (Department d : all) {
            DepartmentTreeDTO node = departmentMapper.toTreeDTO(d);
            node.setChildren(new ArrayList<>());
            nodeMap.put(d.getId(), node);
        }
        List<DepartmentTreeDTO> roots = new ArrayList<>();
        for (Department d : all) {
            DepartmentTreeDTO node = nodeMap.get(d.getId());
            if (d.getParentId() != null && nodeMap.containsKey(d.getParentId())) {
                childrenMap.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(node);
            } else {
                roots.add(node);
            }
        }
        for (Map.Entry<Long, List<DepartmentTreeDTO>> e : childrenMap.entrySet()) {
            nodeMap.get(e.getKey()).setChildren(e.getValue());
        }
        return roots;
    }

    @Override
    public List<DepartmentDTO> listSubDepartments(Long parentId) {
        requireTenant();
        return departmentMapper.toDTOList(departmentRepository.findByParentIdAndDeletedAtIsNull(parentId));
    }

    @Override
    @Transactional
    public void moveDepartment(Long deptId, Long newParentId) {
        requireTenant();
        Department dept = findDept(deptId);
        Long oldParentId = dept.getParentId();
        if (newParentId != null) {
            Department newParent = findDept(newParentId);
            dept.setParentId(newParentId);
            dept.setLevel((newParent.getLevel() == null ? 1 : newParent.getLevel()) + 1);
            dept.setPath(newParent.getPath() + "/" + dept.getId());
        } else {
            dept.setParentId(null);
            dept.setLevel(1);
            dept.setPath("/" + dept.getId());
        }
        dept.setUpdatedBy(TenantContext.getCurrentUserId());
        departmentRepository.save(dept);
        DepartmentMovedEvent movedEvent = new DepartmentMovedEvent();
        movedEvent.setDeptId(deptId);
        movedEvent.setOldParentId(oldParentId);
        movedEvent.setNewParentId(newParentId);
        eventPublisher.publish(movedEvent);
    }

    @Override
    public PageResult<UserBriefDTO> listDepartmentUsers(Long deptId, PageQuery query) {
        requireTenant();
        List<Long> userIds = userPositionRepository.findByDeptId(deptId).stream()
                .map(u -> u.getUserId()).distinct().toList();
        int total = userIds.size();
        int from = (query.getPage() - 1) * query.getSize();
        int to = Math.min(from + query.getSize(), total);
        String deptName = departmentRepository.findByIdAndDeletedAtIsNull(deptId)
                .map(Department::getDeptName).orElse(null);
        List<UserBriefDTO> list = userIds.subList(Math.max(from, 0), Math.max(to, 0)).stream()
                .map(uid -> toBrief(uid, deptId, deptName)).toList();
        return PageResult.of(list, total);
    }

    private UserBriefDTO toBrief(Long userId, Long deptId, String deptName) {
        UserBriefDTO dto = new UserBriefDTO();
        dto.setId(userId);
        dto.setDeptId(deptId);
        dto.setDeptName(deptName);
        try {
            var user = userService.getUserById(userId);
            dto.setUsername(user.getUsername());
            dto.setRealName(user.getRealName());
        } catch (Exception ignored) {
        }
        return dto;
    }

    private Department findDept(Long deptId) {
        return departmentRepository.findByIdAndDeletedAtIsNull(deptId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.DEPT_NOT_FOUND, String.valueOf(deptId)));
    }

    private void requireTenant() {
        if (TenantContext.getTenantId() == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "未确定租户上下文");
        }
    }
}
