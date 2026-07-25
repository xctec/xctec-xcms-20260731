package com.df4j.xctec.xcms.config.repository;

import com.df4j.xctec.xcms.config.domain.CodeRule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CodeRuleRepository extends JpaRepository<CodeRule, Long> {

    Optional<CodeRule> findByRuleCode(String ruleCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from CodeRule r where r.ruleCode = :code")
    Optional<CodeRule> findByRuleCodeForUpdate(@Param("code") String code);
}
