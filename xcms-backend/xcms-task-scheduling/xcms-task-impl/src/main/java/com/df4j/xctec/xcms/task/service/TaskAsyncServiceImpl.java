package com.df4j.xctec.xcms.task.service;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.task.api.TaskAsyncService;
import com.df4j.xctec.xcms.task.api.dto.AsyncTaskDTO;
import com.df4j.xctec.xcms.task.api.dto.request.AsyncTaskQuery;
import com.df4j.xctec.xcms.task.api.dto.request.AsyncTaskRequest;
import com.df4j.xctec.xcms.task.domain.TaskAsync;
import com.df4j.xctec.xcms.task.repository.TaskAsyncRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskAsyncServiceImpl implements TaskAsyncService {

    private final TaskAsyncRepository asyncRepository;

    @Override
    @Transactional
    public AsyncTaskDTO submit(AsyncTaskRequest request) {
        Long tenantId = request.getTenantId() != null ? request.getTenantId() : TenantContext.getTenantId();
        TaskAsync t = new TaskAsync();
        t.setTenantId(tenantId);
        t.setTaskType(request.getTaskType());
        t.setPayload(request.getPayload());
        t.setPriority(request.getPriority());
        t.setScheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : LocalDateTime.now());
        t.setStatus("PENDING");
        t.setRetryCount(0);
        return toDto(asyncRepository.save(t));
    }

    @Override
    public AsyncTaskDTO get(Long id) {
        return toDto(asyncRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "异步任务不存在: " + id)));
    }

    @Override
    public PageResult<AsyncTaskDTO> list(AsyncTaskQuery query) {
        Specification<TaskAsync> spec = (root, cq, cb) -> {
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
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<TaskAsync> page = asyncRepository.findAll(spec,
                PageRequest.of(query.getPage() - 1, query.getSize()));
        List<AsyncTaskDTO> list = page.getContent().stream().map(this::toDto).toList();
        return PageResult.of(list, page.getTotalElements());
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        TaskAsync t = asyncRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "异步任务不存在: " + id));
        if (!"PENDING".equals(t.getStatus())) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "仅 PENDING 状态可取消，当前: " + t.getStatus());
        }
        int n = asyncRepository.updateStatusIfExpected(id, "CANCELLED", "PENDING");
        if (n != 1) {
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "取消失败");
        }
    }

    private AsyncTaskDTO toDto(TaskAsync t) {
        AsyncTaskDTO dto = new AsyncTaskDTO();
        dto.setId(t.getId());
        dto.setTenantId(t.getTenantId());
        dto.setTaskType(t.getTaskType());
        dto.setPayload(t.getPayload());
        dto.setStatus(t.getStatus());
        dto.setPriority(t.getPriority());
        dto.setScheduledAt(t.getScheduledAt());
        dto.setStartedAt(t.getStartedAt());
        dto.setCompletedAt(t.getCompletedAt());
        dto.setRetryCount(t.getRetryCount());
        dto.setErrorMsg(t.getErrorMsg());
        return dto;
    }
}
