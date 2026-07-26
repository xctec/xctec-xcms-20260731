package com.df4j.xctec.xcms.operation.service;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.operation.api.HealthCheckService;
import com.df4j.xctec.xcms.operation.api.dto.HealthCheckDTO;
import com.df4j.xctec.xcms.operation.api.dto.HealthCheckLogDTO;
import com.df4j.xctec.xcms.operation.api.dto.request.HealthCheckCreateRequest;
import com.df4j.xctec.xcms.operation.api.dto.request.HealthCheckQuery;
import com.df4j.xctec.xcms.operation.api.dto.request.HealthCheckUpdateRequest;
import com.df4j.xctec.xcms.operation.domain.HealthCheck;
import com.df4j.xctec.xcms.operation.domain.HealthCheckLog;
import com.df4j.xctec.xcms.operation.repository.HealthCheckLogRepository;
import com.df4j.xctec.xcms.operation.repository.HealthCheckRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckServiceImpl implements HealthCheckService {

    private final HealthCheckRepository checkRepository;
    private final HealthCheckLogRepository logRepository;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5)).build();

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public HealthCheckDTO create(HealthCheckCreateRequest request) {
        HealthCheck h = new HealthCheck();
        apply(h, request);
        h.setStatus("ENABLED");
        return toDto(checkRepository.save(h));
    }

    @Override
    @Transactional
    public HealthCheckDTO update(HealthCheckUpdateRequest request) {
        HealthCheck h = getOrThrow(request.getId());
        if (request.getName() != null) {
            h.setName(request.getName());
        }
        if (request.getCheckType() != null) {
            h.setCheckType(request.getCheckType());
        }
        if (request.getTarget() != null) {
            h.setTarget(request.getTarget());
        }
        if (request.getExpectedCode() != null) {
            h.setExpectedCode(request.getExpectedCode());
        }
        if (request.getExpectedValue() != null) {
            h.setExpectedValue(request.getExpectedValue());
        }
        if (request.getIntervalSec() > 0) {
            h.setIntervalSec(request.getIntervalSec());
        }
        if (request.getTimeoutMs() > 0) {
            h.setTimeoutMs(request.getTimeoutMs());
        }
        if (request.getStatus() != null) {
            h.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            h.setDescription(request.getDescription());
        }
        return toDto(checkRepository.save(h));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        checkRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void enable(Long id) {
        HealthCheck h = getOrThrow(id);
        h.setStatus("ENABLED");
        checkRepository.save(h);
    }

    @Override
    @Transactional
    public void disable(Long id) {
        HealthCheck h = getOrThrow(id);
        h.setStatus("DISABLED");
        checkRepository.save(h);
    }

    @Override
    @Transactional
    public void run(Long id) {
        HealthCheck h = getOrThrow(id);
        doCheck(h);
    }

    @Override
    public HealthCheckDTO get(Long id) {
        return toDto(getOrThrow(id));
    }

    @Override
    public PageResult<HealthCheckDTO> list(HealthCheckQuery query) {
        Specification<HealthCheck> spec = (root, cq, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (StringUtils.hasText(query.getStatus())) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            if (StringUtils.hasText(query.getCheckType())) {
                predicates.add(cb.equal(root.get("checkType"), query.getCheckType()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<HealthCheck> page = checkRepository.findAll(spec,
                PageRequest.of(query.getPage() - 1, query.getSize()));
        return PageResult.of(page.getContent().stream().map(this::toDto).toList(), page.getTotalElements());
    }

    @Override
    public PageResult<HealthCheckLogDTO> logs(Long checkId, int page, int size) {
        Page<HealthCheckLog> p = logRepository.findByCheckIdOrderByCheckTimeDesc(checkId,
                PageRequest.of(Math.max(page - 1, 0), size <= 0 ? 20 : size));
        return PageResult.of(p.getContent().stream().map(this::toLogDto).toList(), p.getTotalElements());
    }

    /** 由调度器周期调用：对到期且启用的检查执行探测 */
    @Transactional
    public void runDueChecks() {
        LocalDateTime now = LocalDateTime.now();
        for (HealthCheck h : checkRepository.findByStatus("ENABLED")) {
            if (h.getLastCheckAt() == null
                    || h.getLastCheckAt().plusSeconds(h.getIntervalSec()).isBefore(now)) {
                doCheck(h);
            }
        }
    }

    private void doCheck(HealthCheck h) {
        long start = System.currentTimeMillis();
        boolean up;
        String msg;
        try {
            switch (h.getCheckType()) {
                case "HTTP" -> up = doHttp(h);
                case "TCP", "PING" -> up = doTcp(h);
                case "DB" -> up = doDb(h);
                default -> {
                    up = false;
                    msg = "未知检查类型: " + h.getCheckType();
                    persistLog(h, false, msg, start);
                    return;
                }
            }
            msg = up ? "OK" : "检查未通过";
        } catch (Exception e) {
            up = false;
            msg = e.getMessage();
        }
        persistLog(h, up, msg, start);
    }

    private boolean doHttp(HealthCheck h) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(h.getTarget()))
                .timeout(java.time.Duration.ofMillis(h.getTimeoutMs())).GET().build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (StringUtils.hasText(h.getExpectedCode())) {
            return h.getExpectedCode().equals(String.valueOf(resp.statusCode()));
        }
        return resp.statusCode() >= 200 && resp.statusCode() < 400;
    }

    private boolean doTcp(HealthCheck h) {
        String host = h.getTarget();
        int port = 80;
        if (host.contains(":")) {
            String[] parts = host.split(":", 2);
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        }
        try (Socket s = new Socket()) {
            s.connect(new java.net.InetSocketAddress(host, port), h.getTimeoutMs());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean doDb(HealthCheck h) {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void persistLog(HealthCheck h, boolean up, String msg, long start) {
        int latency = (int) (System.currentTimeMillis() - start);
        HealthCheckLog log = new HealthCheckLog();
        log.setTenantId(h.getTenantId());
        log.setCheckId(h.getId());
        log.setCheckName(h.getName());
        log.setStatus(up ? "UP" : "DOWN");
        log.setLatencyMs(latency);
        log.setMessage(truncate(msg, 1000));
        log.setCheckTime(LocalDateTime.now());
        logRepository.save(log);

        h.setLastStatus(up ? "UP" : "DOWN");
        h.setLastCheckAt(LocalDateTime.now());
        checkRepository.save(h);
    }

    private void apply(HealthCheck h, HealthCheckCreateRequest r) {
        h.setName(r.getName());
        h.setCheckType(r.getCheckType());
        h.setTarget(r.getTarget());
        h.setExpectedCode(r.getExpectedCode());
        h.setExpectedValue(r.getExpectedValue());
        h.setIntervalSec(r.getIntervalSec() > 0 ? r.getIntervalSec() : 60);
        h.setTimeoutMs(r.getTimeoutMs() > 0 ? r.getTimeoutMs() : 5000);
        h.setDescription(r.getDescription());
    }

    private HealthCheck getOrThrow(Long id) {
        return checkRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "健康检查不存在: " + id));
    }

    private HealthCheckDTO toDto(HealthCheck h) {
        HealthCheckDTO dto = new HealthCheckDTO();
        dto.setId(h.getId());
        dto.setTenantId(h.getTenantId());
        dto.setName(h.getName());
        dto.setCheckType(h.getCheckType());
        dto.setTarget(h.getTarget());
        dto.setExpectedCode(h.getExpectedCode());
        dto.setExpectedValue(h.getExpectedValue());
        dto.setIntervalSec(h.getIntervalSec());
        dto.setTimeoutMs(h.getTimeoutMs());
        dto.setStatus(h.getStatus());
        dto.setLastStatus(h.getLastStatus());
        dto.setLastCheckAt(h.getLastCheckAt());
        dto.setDescription(h.getDescription());
        return dto;
    }

    private HealthCheckLogDTO toLogDto(HealthCheckLog l) {
        HealthCheckLogDTO dto = new HealthCheckLogDTO();
        dto.setId(l.getId());
        dto.setTenantId(l.getTenantId());
        dto.setCheckId(l.getCheckId());
        dto.setCheckName(l.getCheckName());
        dto.setStatus(l.getStatus());
        dto.setLatencyMs(l.getLatencyMs());
        dto.setMessage(l.getMessage());
        dto.setCheckTime(l.getCheckTime());
        return dto;
    }

    private String truncate(String s, int max) {
        return s == null ? null : (s.length() <= max ? s : s.substring(0, max));
    }
}
