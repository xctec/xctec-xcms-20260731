package com.df4j.xctec.xcms.task.repository;

import com.df4j.xctec.xcms.task.domain.TaskAsync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskAsyncRepository extends JpaRepository<TaskAsync, Long>,
        JpaSpecificationExecutor<TaskAsync> {

    @Query("SELECT t FROM TaskAsync t WHERE t.status = 'PENDING' AND t.scheduledAt <= :now "
            + "ORDER BY t.priority DESC, t.scheduledAt ASC")
    List<TaskAsync> findDuePending(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE TaskAsync t SET t.status = :status WHERE t.id = :id AND t.status = :expected")
    int updateStatusIfExpected(@Param("id") Long id, @Param("status") String status,
                               @Param("expected") String expected);
}
