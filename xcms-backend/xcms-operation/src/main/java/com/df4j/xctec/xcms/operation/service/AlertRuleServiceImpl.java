package com.df4j.xctec.xcms.operation.service;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.message.api.MessageService;
import com.df4j.xctec.xcms.message.api.dto.request.SendMessageCommand;
import com.df4j.xctec.xcms.operation.api.AlertRuleService;
import com.df4j.xctec.xcms.operation.api.dto.AlertRecordDTO;
import com.df4j.xctec.xcms.operation.api.dto.AlertRuleDTO;
import com.df4j.xctec.xcms.operation.api.dto.request.AlertRuleCreateRequest;
import com.df4j.xctec.xcms.operation.api.dto.request.AlertRuleQuery;
import com.df4j.xctec.xcms.operation.api.dto.request.AlertRuleUpdateRequest;
import com.df4j.xctec.xcms.operation.domain.AlertRecord;
import com.df4j.xctec.xcms.operation.domain.AlertRule;
import com.df4j.xctec.xcms.operation.repository.AlertRecordRepository;
import com.df4j.xctec.xcms.operation.repository.AlertRuleRepository;
import com.df4j.xctec.xcms.operation.service.MetricServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleRepository ruleRepository;
    private final AlertRecordRepository recordRepository;
    private final MetricServiceImpl metricService;
    private final ObjectProvider<MessageService> messageService;

    @Override
    @Transactional
    public AlertRuleDTO create(AlertRuleCreateRequest request) {
        AlertRule r = new AlertRule();
        apply(r, request);
        r.setEnabled(request.isEnabled());
        return toDto(ruleRepository.save(r));
    }

    @Override
    @Transactional
    public AlertRuleDTO update(AlertRuleUpdateRequest request) {
        AlertRule r = getOrThrow(request.getId());
        if (request.getRuleName() != null) {
            r.setRuleName(request.getRuleName());
        }
        if (request.getMetricKey() != null) {
            r.setMetricKey(request.getMetricKey());
        }
        if (request.getCondition() != null) {
            r.setCondition(request.getCondition());
        }
        if (request.getThreshold() != null) {
            r.setThreshold(request.getThreshold());
        }
        if (request.getDurationMin() > 0) {
            r.setDurationMin(request.getDurationMin());
        }
        if (request.getSeverity() != null) {
            r.setSeverity(request.getSeverity());
        }
        if (request.getChannel() != null) {
            r.setChannel(request.getChannel());
        }
        if (request.getEnabled() != null) {
            r.setEnabled(request.getEnabled());
        }
        if (request.getDescription() != null) {
            r.setDescription(request.getDescription());
        }
        return toDto(ruleRepository.save(r));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        ruleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void enable(Long id) {
        AlertRule r = getOrThrow(id);
        r.setEnabled(true);
        ruleRepository.save(r);
    }

    @Override
    @Transactional
    public void disable(Long id) {
        AlertRule r = getOrThrow(id);
        r.setEnabled(false);
        ruleRepository.save(r);
    }

    @Override
    public AlertRuleDTO get(Long id) {
        return toDto(getOrThrow(id));
    }

    @Override
    public PageResult<AlertRuleDTO> list(AlertRuleQuery query) {
        Specification<AlertRule> spec = (root, cq, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (query.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), query.getEnabled()));
            }
            if (StringUtils.hasText(query.getMetricKey())) {
                predicates.add(cb.equal(root.get("metricKey"), query.getMetricKey()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<AlertRule> page = ruleRepository.findAll(spec,
                PageRequest.of(query.getPage() - 1, query.getSize()));
        return PageResult.of(page.getContent().stream().map(this::toDto).toList(), page.getTotalElements());
    }

    @Override
    @Transactional
    public void evaluate() {
        for (AlertRule r : ruleRepository.findByEnabledTrue()) {
            try {
                var latest = metricService.getLatest(r.getMetricKey());
                if (latest == null || latest.getValue() == null) {
                    continue;
                }
                boolean breached = match(r.getCondition(), latest.getValue(), r.getThreshold());
                if (breached) {
                    AlertRecord rec = new AlertRecord();
                    rec.setTenantId(r.getTenantId());
                    rec.setRuleId(r.getId());
                    rec.setRuleName(r.getRuleName());
                    rec.setMetricKey(r.getMetricKey());
                    rec.setMetricValue(latest.getValue());
                    rec.setMessage("指标 " + r.getMetricKey() + " 当前值 " + latest.getValue()
                            + " " + r.getCondition() + " 阈值 " + r.getThreshold());
                    rec.setTriggeredAt(LocalDateTime.now());
                    rec.setStatus("OPEN");
                    recordRepository.save(rec);

                    r.setLastTriggeredAt(LocalDateTime.now());
                    ruleRepository.save(r);

                    notify(r, rec.getMessage());
                }
            } catch (Exception e) {
                log.warn("[ops] evaluate alert rule {} failed", r.getId(), e);
            }
        }
    }

    @Override
    public PageResult<AlertRecordDTO> records(int page, int size) {
        Page<AlertRecord> p = recordRepository.findAllByOrderByTriggeredAtDesc(
                PageRequest.of(Math.max(page - 1, 0), size <= 0 ? 20 : size));
        return PageResult.of(p.getContent().stream().map(this::toRecordDto).toList(), p.getTotalElements());
    }

    private boolean match(String condition, BigDecimal value, BigDecimal threshold) {
        int c = value.compareTo(threshold);
        return switch (condition) {
            case ">" -> c > 0;
            case ">=" -> c >= 0;
            case "<" -> c < 0;
            case "<=" -> c <= 0;
            default -> false;
        };
    }

    private void notify(AlertRule r, String message) {
        MessageService svc = messageService.getIfAvailable();
        if (svc == null) {
            return;
        }
        try {
            SendMessageCommand cmd = new SendMessageCommand();
            cmd.setMsgType("NOTICE");
            cmd.setTitle("告警[" + r.getSeverity() + "]: " + r.getRuleName());
            cmd.setContent(message);
            cmd.setRecipientIds(new ArrayList<>());
            svc.send(cmd);
        } catch (Exception e) {
            log.warn("[ops] send alert notification failed", e);
        }
    }

    private void apply(AlertRule r, AlertRuleCreateRequest req) {
        r.setRuleName(req.getRuleName());
        r.setMetricKey(req.getMetricKey());
        r.setCondition(req.getCondition());
        r.setThreshold(req.getThreshold());
        r.setDurationMin(req.getDurationMin());
        r.setSeverity(req.getSeverity());
        r.setChannel(req.getChannel());
        r.setDescription(req.getDescription());
    }

    private AlertRule getOrThrow(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "告警规则不存在: " + id));
    }

    private AlertRuleDTO toDto(AlertRule r) {
        AlertRuleDTO dto = new AlertRuleDTO();
        dto.setId(r.getId());
        dto.setTenantId(r.getTenantId());
        dto.setRuleName(r.getRuleName());
        dto.setMetricKey(r.getMetricKey());
        dto.setCondition(r.getCondition());
        dto.setThreshold(r.getThreshold());
        dto.setDurationMin(r.getDurationMin());
        dto.setSeverity(r.getSeverity());
        dto.setChannel(r.getChannel());
        dto.setEnabled(r.isEnabled());
        dto.setLastTriggeredAt(r.getLastTriggeredAt());
        dto.setDescription(r.getDescription());
        return dto;
    }

    private AlertRecordDTO toRecordDto(AlertRecord r) {
        AlertRecordDTO dto = new AlertRecordDTO();
        dto.setId(r.getId());
        dto.setTenantId(r.getTenantId());
        dto.setRuleId(r.getRuleId());
        dto.setRuleName(r.getRuleName());
        dto.setMetricKey(r.getMetricKey());
        dto.setMetricValue(r.getMetricValue());
        dto.setMessage(r.getMessage());
        dto.setTriggeredAt(r.getTriggeredAt());
        dto.setStatus(r.getStatus());
        dto.setResolvedAt(r.getResolvedAt());
        return dto;
    }
}
