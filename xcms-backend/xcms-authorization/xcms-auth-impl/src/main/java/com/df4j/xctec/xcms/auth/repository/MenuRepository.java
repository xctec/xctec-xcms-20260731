package com.df4j.xctec.xcms.auth.repository;

import com.df4j.xctec.xcms.auth.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByParentIdIsNullOrderBySortOrderAsc();

    List<Menu> findByParentIdOrderBySortOrderAsc(Long parentId);

    java.util.Optional<Menu> findByMenuCode(String menuCode);

    List<Menu> findByMenuType(String menuType);

    List<Menu> findByIdIn(List<Long> ids);
}
