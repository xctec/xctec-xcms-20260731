package com.df4j.xctec.xcms.auth.api;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface DataPermissionService {

    <T> Specification<T> getDataScopeSpec(Long userId, String resourceType);

    DataPermissionContext getDataPermissionContext(Long userId, String resourceType);

    <T> T applyColumnMask(T entity, Long userId, String resourceType);

    <T> List<T> applyColumnMask(List<T> entities, Long userId, String resourceType);
}
