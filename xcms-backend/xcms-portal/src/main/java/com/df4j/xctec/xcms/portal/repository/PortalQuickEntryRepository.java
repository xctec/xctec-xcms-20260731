package com.df4j.xctec.xcms.portal.repository;

import com.df4j.xctec.xcms.portal.domain.PortalQuickEntry;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户快捷入口仓储。租户隔离由 {@code @TenantId} 自动注入。
 */
@Repository
public interface PortalQuickEntryRepository extends JpaRepository<PortalQuickEntry, Long> {

    List<PortalQuickEntry> findByUserIdAndSurfaceAndDeletedAtIsNull(Long userId, String surface, Sort sort);

    List<PortalQuickEntry> findByUserIdAndDeletedAtIsNull(Long userId, Sort sort);
}
