package com.df4j.xctec.xcms.org.repository;

import com.df4j.xctec.xcms.org.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByParentIdAndDeletedAtIsNull(Long parentId);

    List<Department> findByParentIdIsNullAndDeletedAtIsNull();

    Optional<Department> findByIdAndDeletedAtIsNull(Long id);

    List<Department> findByDeletedAtIsNullOrderBySortOrderAsc();

    boolean existsByDeptCodeAndDeletedAtIsNull(String deptCode);
}
