package com.df4j.xctec.xcms.org.repository;

import com.df4j.xctec.xcms.org.domain.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByParentIdAndDeletedAtIsNull(Long parentId);

    List<Department> findByParentIdIsNullAndDeletedAtIsNull();

    /** 显式带 tenantId 的查询：租户初始化等跨租户场景使用，不依赖隐式 @TenantId 过滤 */
    List<Department> findByTenantIdAndParentIdIsNullAndDeletedAtIsNull(Long tenantId);

    Optional<Department> findByIdAndDeletedAtIsNull(Long id);

    List<Department> findByDeletedAtIsNullOrderBySortOrderAsc();

    boolean existsByDeptCodeAndDeletedAtIsNull(String deptCode);

    @Query("select d from Department d where d.path like concat(?1, '%') and d.deletedAt is null")
    List<Department> findByPathStartsWith(String pathPrefix);
}
