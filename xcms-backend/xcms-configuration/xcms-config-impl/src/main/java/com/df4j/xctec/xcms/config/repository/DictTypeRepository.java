package com.df4j.xctec.xcms.config.repository;

import com.df4j.xctec.xcms.config.domain.DictType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DictTypeRepository extends JpaRepository<DictType, Long> {

    Optional<DictType> findByDictCode(String dictCode);
}
