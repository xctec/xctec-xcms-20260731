package com.df4j.xctec.xcms.config.repository;

import com.df4j.xctec.xcms.config.domain.ConfigSwitch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigSwitchRepository extends JpaRepository<ConfigSwitch, Long> {
    Optional<ConfigSwitch> findBySwitchKey(String switchKey);
}
