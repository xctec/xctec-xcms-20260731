package com.df4j.xctec.xcms.audit.service;

import com.df4j.xctec.xcms.audit.api.AuditService;
import com.df4j.xctec.xcms.audit.api.dto.AuditLogDTO;
import com.df4j.xctec.xcms.audit.api.dto.AuditQuery;
import com.df4j.xctec.xcms.audit.domain.AuditLog;
import com.df4j.xctec.xcms.audit.repository.AuditLogRepository;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResult<AuditLogDTO> query(AuditQuery query) {
        Specification<AuditLog> spec = (root, cq, cb) -> {
            List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
            if (StringUtils.hasText(query.getBizModule())) {
                ps.add(cb.equal(root.get("bizModule"), query.getBizModule()));
            }
            if (StringUtils.hasText(query.getBizType())) {
                ps.add(cb.equal(root.get("bizType"), query.getBizType()));
            }
            if (query.getOperatorId() != null) {
                ps.add(cb.equal(root.get("operatorId"), query.getOperatorId()));
            }
            if (query.getSuccess() != null) {
                ps.add(cb.equal(root.get("success"), query.getSuccess()));
            }
            if (StringUtils.hasText(query.getBizId())) {
                ps.add(cb.equal(root.get("bizId"), query.getBizId()));
            }
            return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        int page = query.getPage() <= 0 ? 1 : query.getPage();
        int size = query.getSize() <= 0 ? 20 : query.getSize();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AuditLog> result = auditLogRepository.findAll(spec, pageable);
        List<AuditLogDTO> list = result.getContent().stream().map(this::toDto).toList();
        return PageResult.of(list, result.getTotalElements());
    }

    private AuditLogDTO toDto(AuditLog m) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(m.getId());
        dto.setBizModule(m.getBizModule());
        dto.setBizType(m.getBizType());
        dto.setBizId(m.getBizId());
        dto.setAction(m.getAction());
        dto.setOperatorId(m.getOperatorId());
        dto.setOperatorName(m.getOperatorName());
        dto.setIp(m.getIp());
        dto.setSuccess(m.isSuccess());
        dto.setErrorMsg(m.getErrorMsg());
        dto.setDetail(m.getDetail());
        dto.setDurationMs(m.getDurationMs());
        dto.setOccurTime(m.getOccurTime());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
