package com.df4j.xctec.xcms.task.engine;

import com.df4j.xctec.xcms.task.domain.TaskLock;
import com.df4j.xctec.xcms.task.repository.TaskLockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 调度器集群选主。基于 task_lock 单行（lock_key='scheduler'）记录当前 Leader 实例。
 * 任一时刻仅 Leader 实例负责注册并执行定时任务。
 */
@Slf4j
@Service
public class TaskLockService {

    private static final String KEY = "scheduler";

    private final TaskLockRepository repo;
    private final String instanceId = UUID.randomUUID().toString();

    public TaskLockService(TaskLockRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public boolean tryAcquire(long leaseSeconds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime exp = now.plusSeconds(leaseSeconds);
        TaskLock lock = repo.findByLockKey(KEY).orElse(null);
        if (lock == null) {
            try {
                repo.save(new TaskLock(KEY, instanceId, now, exp));
                return true;
            } catch (DataIntegrityViolationException e) {
                return isLeader();
            }
        }
        if (instanceId.equals(lock.getOwnerId()) || lock.getExpiresAt().isBefore(now)) {
            lock.setOwnerId(instanceId);
            lock.setAcquiredAt(now);
            lock.setExpiresAt(exp);
            repo.save(lock);
            return true;
        }
        return false;
    }

    public boolean isLeader() {
        return repo.findByLockKey(KEY).map(l -> instanceId.equals(l.getOwnerId())).orElse(false);
    }

    @Transactional
    public void renew(long leaseSeconds) {
        repo.findByLockKey(KEY).ifPresent(l -> {
            l.setOwnerId(instanceId);
            l.setAcquiredAt(LocalDateTime.now());
            l.setExpiresAt(LocalDateTime.now().plusSeconds(leaseSeconds));
            repo.save(l);
        });
    }

    @Transactional
    public void release() {
        repo.findByLockKey(KEY).ifPresent(l -> {
            if (instanceId.equals(l.getOwnerId())) {
                repo.delete(l);
            }
        });
    }

    public String getInstanceId() {
        return instanceId;
    }
}
