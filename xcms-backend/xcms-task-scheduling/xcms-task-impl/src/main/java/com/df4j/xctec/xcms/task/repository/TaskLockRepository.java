package com.df4j.xctec.xcms.task.repository;

import com.df4j.xctec.xcms.task.domain.TaskLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskLockRepository extends JpaRepository<TaskLock, String> {

    Optional<TaskLock> findByLockKey(String lockKey);
}
