package com.df4j.xctec.xcms.config.repository;

import com.df4j.xctec.xcms.config.domain.ConfigParam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigParamRepository extends JpaRepository<ConfigParam, Long> {

    Optional<ConfigParam> findByParamKey(String paramKey);
}
