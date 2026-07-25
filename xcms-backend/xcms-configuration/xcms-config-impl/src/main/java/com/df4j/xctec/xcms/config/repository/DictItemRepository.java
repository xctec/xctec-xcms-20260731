package com.df4j.xctec.xcms.config.repository;

import com.df4j.xctec.xcms.config.domain.DictItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DictItemRepository extends JpaRepository<DictItem, Long> {
    List<DictItem> findByTypeCode(String typeCode);

    Optional<DictItem> findByTypeCodeAndItemCode(String typeCode, String itemCode);
}
