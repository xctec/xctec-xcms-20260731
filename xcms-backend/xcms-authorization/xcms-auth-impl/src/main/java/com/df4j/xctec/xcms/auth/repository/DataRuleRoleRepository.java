package com.df4j.xctec.xcms.auth.repository;

import com.df4j.xctec.xcms.auth.domain.DataRuleRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataRuleRoleRepository extends JpaRepository<DataRuleRole, Long> {

    List<DataRuleRole> findByRuleId(Long ruleId);

    List<DataRuleRole> findByRoleId(Long roleId);

    List<DataRuleRole> findByRoleIdIn(List<Long> roleIds);

    void deleteByRuleId(Long ruleId);

    void deleteByRuleIdAndRoleId(Long ruleId, Long roleId);
}
