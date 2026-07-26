package com.df4j.xctec.xcms.operation.service;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.operation.api.OperationLogService;
import com.df4j.xctec.xcms.operation.api.dto.OperationLogDTO;
import com.df4j.xctec.xcms.operation.api.dto.request.OperationLogQuery;
import com.df4j.xctec.xcms.operation.domain.OperationLog;
import com.df4j.xctec.xcms.operation.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository logRepository;

    @Override
    @Transactional
    public void record(String module, String action, String bizType, String bizId,
                       String detail, boolean success, String errorMsg, Long durationMs) {
        try {
            OperationLog log = new OperationLog();
            log.setTenantId(TenantContext.getTenantId());
            log.setOperatorId(TenantContext.getCurrentUserId());
            log.setModule(module);
            log.setAction(action);
            log.setBizType(bizType);
            log.setBizId(bizId);
            log.setDetail(detail);
            log.setResult(success ? "SUCCESS" : "FAIL");
            log.setErrorMsg(errorMsg);
            log.setDurationMs(durationMs);
            log.setOccurTime(LocalDateTime.now());
            logRepository.save(log);
        } catch (Exception e) {
            log.warn("[ops] record operation log failed", e);
        }
    }

    @Override
    public OperationLogDTO get(Long id) {
        return toDto(logRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "操作日志不存在: " + id)));
    }

    @Override
    public PageResult<OperationLogDTO> list(OperationLogQuery query) {
        Specification<OperationLog> spec = (root, cq, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (query.getOperatorId() != null) {
                predicates.add(cb.equal(root.get("operatorId"), query.getOperatorId()));
            }
            if (StringUtils.hasText(query.getModule())) {
                predicates.add(cb.equal(root.get("module"), query.getModule()));
            }
            if (StringUtils.hasText(query.getResult())) {
                predicates.add(cb.equal(root.get("result"), query.getResult()));
            }
            if (query.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurTime"), query.getFrom()));
            }
            if (query.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("occurTime"), query.getTo()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<OperationLog> page = logRepository.findAll(spec,
                PageRequest.of(query.getPage() - 1, query.getSize()));
        return PageResult.of(page.getContent().stream().map(this::toDto).toList(), page.getTotalElements());
    }

    private OperationLogDTO toDto(OperationLog l) {
        OperationLogDTO dto = new OperationLogDTO();
        dto.setId(l.getId());
        dto.setTenantId(l.getTenantId());
        dto.setOperatorId(l.getOperatorId());
        dto.setOperatorName(l.getOperatorName());
        dto.setAction(l.getAction());
        dto.setModule(l.getModule());
        dto.setBizType(l.getBizType());
        dto.setBizId(l.getBizId());
        dto.setIp(l.getIp());
        dto.setDetail(l.getDetail());
        dto.setResult(l.getResult());
        dto.setErrorMsg(l.getErrorMsg());
        dto.setDurationMs(l.getDurationMs());
        dto.setOccurTime(l.getOccurTime());
        return dto;
    }
}
