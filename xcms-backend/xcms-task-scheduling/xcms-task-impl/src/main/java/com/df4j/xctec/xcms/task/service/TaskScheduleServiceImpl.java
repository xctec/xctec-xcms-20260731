package com.df4j.xctec.xcms.task.service;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.task.api.TaskScheduleService;
import com.df4j.xctec.xcms.task.api.dto.TaskExecutionLogDTO;
import com.df4j.xctec.xcms.task.api.dto.TaskScheduleDTO;
import com.df4j.xctec.xcms.task.api.dto.request.TaskCreateRequest;
import com.df4j.xctec.xcms.task.api.dto.request.TaskQuery;
import com.df4j.xctec.xcms.task.api.dto.request.TaskUpdateRequest;
import com.df4j.xctec.xcms.task.domain.TaskExecutionLog;
import com.df4j.xctec.xcms.task.domain.TaskSchedule;
import com.df4j.xctec.xcms.task.engine.TaskSchedulerEngine;
import com.df4j.xctec.xcms.task.repository.TaskExecutionLogRepository;
import com.df4j.xctec.xcms.task.repository.TaskScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskScheduleServiceImpl implements TaskScheduleService {

    private final TaskScheduleRepository scheduleRepository;
    private final TaskExecutionLogRepository logRepository;
    private final TaskSchedulerEngine engine;

    @Override
    @Transactional
    public TaskScheduleDTO createTask(TaskCreateRequest request) {
        Long tenantId = resolveTenant(request.getTenantId());
        if (scheduleRepository.existsByTaskCodeAndTenantId(request.getTaskCode(), tenantId)) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, "任务编码已存在: " + request.getTaskCode());
        }
        TaskSchedule s = new TaskSchedule();
        s.setTenantId(tenantId);
        s.setTaskName(request.getTaskName());
        s.setTaskCode(request.getTaskCode());
        s.setTaskType(request.getTaskType());
        s.setCronExpression(request.getCronExpression());
        s.setFixedRate(request.getFixedRate());
        s.setHandlerName(request.getHandlerName());
        s.setHandlerParams(request.getHandlerParams());
        s.setDescription(request.getDescription());
        s.setStatus("ENABLED");
        s = scheduleRepository.save(s);
        engine.scheduleTask(s);
        return toDto(s);
    }

    @Override
    @Transactional
    public TaskScheduleDTO updateTask(Long taskId, TaskUpdateRequest request) {
        TaskSchedule s = getOrThrow(taskId);
        if (request.getTaskName() != null) {
            s.setTaskName(request.getTaskName());
        }
        if (request.getTaskType() != null) {
            s.setTaskType(request.getTaskType());
        }
        if (request.getCronExpression() != null) {
            s.setCronExpression(request.getCronExpression());
        }
        if (request.getFixedRate() != null) {
            s.setFixedRate(request.getFixedRate());
        }
        if (request.getHandlerName() != null) {
            s.setHandlerName(request.getHandlerName());
        }
        if (request.getHandlerParams() != null) {
            s.setHandlerParams(request.getHandlerParams());
        }
        if (request.getDescription() != null) {
            s.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            s.setStatus(request.getStatus());
        }
        s = scheduleRepository.save(s);
        engine.unscheduleOne(taskId);
        if ("ENABLED".equals(s.getStatus())) {
            engine.scheduleTask(s);
        }
        return toDto(s);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        getOrThrow(taskId);
        engine.unscheduleOne(taskId);
        scheduleRepository.deleteById(taskId);
    }

    @Override
    @Transactional
    public void enableTask(Long taskId) {
        TaskSchedule s = getOrThrow(taskId);
        s.setStatus("ENABLED");
        s = scheduleRepository.save(s);
        engine.scheduleTask(s);
    }

    @Override
    @Transactional
    public void disableTask(Long taskId) {
        TaskSchedule s = getOrThrow(taskId);
        s.setStatus("DISABLED");
        scheduleRepository.save(s);
        engine.unscheduleOne(taskId);
    }

    @Override
    @Transactional
    public void triggerTask(Long taskId) {
        getOrThrow(taskId);
        engine.triggerNow(taskId);
    }

    @Override
    public TaskScheduleDTO getTask(Long taskId) {
        return toDto(getOrThrow(taskId));
    }

    @Override
    public PageResult<TaskScheduleDTO> listTasks(TaskQuery query) {
        Specification<TaskSchedule> spec = (root, cq, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (query.getTenantId() != null) {
                predicates.add(cb.equal(root.get("tenantId"), query.getTenantId()));
            }
            if (StringUtils.hasText(query.getTaskType())) {
                predicates.add(cb.equal(root.get("taskType"), query.getTaskType()));
            }
            if (StringUtils.hasText(query.getStatus())) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            if (StringUtils.hasText(query.getKeyword())) {
                String like = "%" + query.getKeyword() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("taskName"), like),
                        cb.like(root.get("taskCode"), like)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<TaskSchedule> page = scheduleRepository.findAll(spec,
                PageRequest.of(query.getPage() - 1, query.getSize()));
        List<TaskScheduleDTO> list = page.getContent().stream().map(this::toDto).toList();
        return PageResult.of(list, page.getTotalElements());
    }

    @Override
    public PageResult<TaskExecutionLogDTO> listExecutionLogs(Long taskId, int page, int size) {
        Page<TaskExecutionLog> p = logRepository.findByTaskIdOrderByStartTimeDesc(taskId,
                PageRequest.of(Math.max(page - 1, 0), size <= 0 ? 20 : size));
        List<TaskExecutionLogDTO> list = p.getContent().stream().map(this::toLogDto).toList();
        return PageResult.of(list, p.getTotalElements());
    }

    private TaskSchedule getOrThrow(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "任务不存在: " + id));
    }

    private Long resolveTenant(Long reqTenant) {
        if (reqTenant != null) {
            return reqTenant;
        }
        Long ctx = TenantContext.getTenantId();
        return ctx != null ? ctx : 0L;
    }

    private TaskScheduleDTO toDto(TaskSchedule s) {
        TaskScheduleDTO dto = new TaskScheduleDTO();
        dto.setId(s.getId());
        dto.setTenantId(s.getTenantId());
        dto.setTaskName(s.getTaskName());
        dto.setTaskCode(s.getTaskCode());
        dto.setTaskType(s.getTaskType());
        dto.setCronExpression(s.getCronExpression());
        dto.setFixedRate(s.getFixedRate());
        dto.setHandlerName(s.getHandlerName());
        dto.setHandlerParams(s.getHandlerParams());
        dto.setStatus(s.getStatus());
        dto.setLastExecAt(s.getLastExecAt());
        dto.setNextExecAt(s.getNextExecAt());
        dto.setDescription(s.getDescription());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());
        return dto;
    }

    private TaskExecutionLogDTO toLogDto(TaskExecutionLog l) {
        TaskExecutionLogDTO dto = new TaskExecutionLogDTO();
        dto.setId(l.getId());
        dto.setTenantId(l.getTenantId());
        dto.setTaskId(l.getTaskId());
        dto.setTaskName(l.getTaskName());
        dto.setStartTime(l.getStartTime());
        dto.setEndTime(l.getEndTime());
        dto.setStatus(l.getStatus());
        dto.setResult(l.getResult());
        dto.setErrorMsg(l.getErrorMsg());
        dto.setRetryCount(l.getRetryCount());
        return dto;
    }
}
