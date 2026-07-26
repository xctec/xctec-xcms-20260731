package com.df4j.xctec.xcms.operation.repository;

import com.df4j.xctec.xcms.operation.domain.AlertRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRecordRepository extends JpaRepository<AlertRecord, Long>,
        JpaSpecificationExecutor<AlertRecord> {

    Page<AlertRecord> findAllByOrderByTriggeredAtDesc(Pageable pageable);
}
