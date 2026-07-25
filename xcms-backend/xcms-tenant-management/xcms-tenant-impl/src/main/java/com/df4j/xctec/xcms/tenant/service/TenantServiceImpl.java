package com.df4j.xctec.xcms.tenant.service;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.kernel.exception.NotFoundException;
import com.df4j.xctec.xcms.tenant.api.TenantFeatureService;
import com.df4j.xctec.xcms.tenant.api.TenantQuotaService;
import com.df4j.xctec.xcms.tenant.api.TenantService;
import com.df4j.xctec.xcms.tenant.api.dto.QuotaAllocateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantCreateRequest;
import com.df4j.xctec.xcms.tenant.api.dto.TenantDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantQuery;
import com.df4j.xctec.xcms.tenant.api.dto.TenantTreeDTO;
import com.df4j.xctec.xcms.tenant.api.dto.TenantUpdateRequest;
import com.df4j.xctec.xcms.tenant.api.enums.TenantStatus;
import com.df4j.xctec.xcms.tenant.api.event.TenantCreatedEvent;
import com.df4j.xctec.xcms.tenant.api.event.TenantMigratedEvent;
import com.df4j.xctec.xcms.tenant.api.event.TenantSuspendedEvent;
import com.df4j.xctec.xcms.tenant.domain.TenantInfo;
import com.df4j.xctec.xcms.tenant.mapper.TenantMapper;
import com.df4j.xctec.xcms.tenant.repository.TenantInfoRepository;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租户管理实现
 */
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantInfoRepository tenantInfoRepository;
    private final TenantMapper tenantMapper;
    private final TenantQuotaService tenantQuotaService;
    private final TenantFeatureService tenantFeatureService;
    private final DomainEventPublisher eventPublisher;

    /** 最大租户层级（可配置，默认 5 级） */
    private static final int MAX_TENANT_LEVEL = 5;

    @Override
    @Transactional
    public TenantDTO createTenant(TenantCreateRequest request) {
        if (tenantInfoRepository.existsByTenantCodeAndDeletedAtIsNull(request.getTenantCode())) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, "租户编码已存在: " + request.getTenantCode());
        }
        // 根租户：parentId 为 null 或 0 表示平台根租户，无需有效父节点
        boolean isRoot = request.getParentId() == null || request.getParentId() == 0L;
        TenantInfo parent = null;
        if (!isRoot) {
            parent = tenantInfoRepository.findById(request.getParentId())
                    .filter(p -> p.getDeletedAt() == null)
                    .orElseThrow(() -> new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "父租户不存在: " + request.getParentId()));
        }

        if (parent != null && parent.getLevel() + 1 > MAX_TENANT_LEVEL) {
            throw new BusinessException(ErrorCodes.QUOTA_EXCEEDED,
                    "超过最大租户层级限制: " + MAX_TENANT_LEVEL);
        }

        TenantInfo entity = new TenantInfo();
        entity.setTenantCode(request.getTenantCode());
        entity.setTenantName(request.getTenantName());
        entity.setTenantType(request.getTenantType());
        entity.setParentId(parent != null ? parent.getId() : null);
        entity.setLevel(parent != null ? parent.getLevel() + 1 : 0);
        entity.setDeploymentMode("SHARED");
        entity.setDatasourceKey("shared");
        entity.setStatus(TenantStatus.ACTIVE);
        entity.setCreatedBy(TenantContext.getCurrentUserId());
        // 先保存以获取主键，再回填 path
        TenantInfo saved = tenantInfoRepository.save(entity);
        String parentPath = parent != null ? parent.getPath() : "/";
        saved.setPath(parentPath + saved.getId() + "/");
        TenantInfo persisted = tenantInfoRepository.save(saved);

        // 初始化配额与功能开关
        if (request.getQuotas() != null) {
            for (QuotaAllocateRequest quota : request.getQuotas()) {
                tenantQuotaService.allocateQuota(persisted.getId(), quota);
            }
        }
        if (request.getFeatures() != null) {
            for (Map.Entry<String, Boolean> entry : request.getFeatures().entrySet()) {
                tenantFeatureService.toggleFeature(persisted.getId(), entry.getKey(), entry.getValue());
            }
        }

        TenantCreatedEvent createdEvent = new TenantCreatedEvent();
        createdEvent.setTenantId(persisted.getId());
        createdEvent.setTenantCode(persisted.getTenantCode());
        createdEvent.setTenantName(persisted.getTenantName());
        createdEvent.setTenantType(persisted.getTenantType());
        createdEvent.setParentId(persisted.getParentId());
        eventPublisher.publish(createdEvent);
        return tenantMapper.toDto(persisted);
    }

    @Override
    @Transactional
    public TenantDTO updateTenant(Long tenantId, TenantUpdateRequest request) {
        TenantInfo entity = assertNotMigrating(tenantId);
        if (StringUtils.hasText(request.getTenantName())) {
            entity.setTenantName(request.getTenantName());
        }
        if (request.getStatus() != null) {
            TenantStatus old = entity.getStatus();
            entity.setStatus(request.getStatus());
            if (old != TenantStatus.SUSPENDED && request.getStatus() == TenantStatus.SUSPENDED) {
                TenantSuspendedEvent suspendedEvent = new TenantSuspendedEvent();
                suspendedEvent.setTenantId(tenantId);
                suspendedEvent.setReason("租户被停用");
                eventPublisher.publish(suspendedEvent);
            }
        }
        return tenantMapper.toDto(tenantInfoRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public TenantDTO getTenantById(Long tenantId) {
        return tenantMapper.toDto(requireTenant(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public TenantDTO getTenantByCode(String tenantCode) {
        TenantInfo entity = tenantInfoRepository.findByTenantCode(tenantCode)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "租户不存在: " + tenantCode));
        return tenantMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantTreeDTO> getTenantTree(Long rootTenantId) {
        TenantInfo root = requireTenant(rootTenantId);
        List<TenantInfo> all = tenantInfoRepository.findSubTree(root.getPath());
        Map<Long, List<TenantInfo>> childrenByParent = all.stream()
                .filter(t -> t.getParentId() != null)
                .collect(Collectors.groupingBy(TenantInfo::getParentId));
        return List.of(buildChildren(root, childrenByParent));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<TenantDTO> listSubTenants(Long parentId, TenantQuery query) {
        Specification<TenantInfo> spec = (root, cq, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("parentId"), parentId));
            if (StringUtils.hasText(query.getKeyword())) {
                String like = "%" + query.getKeyword() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("tenantCode"), like),
                        cb.like(root.get("tenantName"), like)));
            }
            if (query.getTenantType() != null) {
                predicates.add(cb.equal(root.get("tenantType"), query.getTenantType()));
            }
            if (query.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Sort sort = StringUtils.hasText(query.getSortBy())
                ? Sort.by(Sort.Direction.fromString(query.getSortOrder()), query.getSortBy())
                : Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageRequest = PageRequest.of(Math.max(query.getPage() - 1, 0), query.getSize(), sort);
        Page<TenantInfo> page = tenantInfoRepository.findAll(spec, pageRequest);
        return PageResult.of(tenantMapper.toDtoList(page.getContent()), page.getTotalElements());
    }

    @Override
    @Transactional
    public void changeTenantStatus(Long tenantId, TenantStatus status) {
        TenantInfo entity = assertNotMigrating(tenantId);
        TenantStatus old = entity.getStatus();
        if (!isTransitionAllowed(old, status)) {
            throw new BusinessException(ErrorCodes.VALIDATION_ERROR,
                    "非法的租户状态转换: " + old + " -> " + status);
        }
        entity.setStatus(status);
        tenantInfoRepository.save(entity);
        if (old != TenantStatus.SUSPENDED && status == TenantStatus.SUSPENDED) {
            TenantSuspendedEvent suspendedEvent = new TenantSuspendedEvent();
            suspendedEvent.setTenantId(tenantId);
            suspendedEvent.setReason("租户被停用");
            eventPublisher.publish(suspendedEvent);
        }
    }

    @Override
    @Transactional
    public void migrateTenant(Long tenantId, Long newParentId) {
        TenantInfo entity = requireTenant(tenantId);
        TenantInfo newParent = tenantInfoRepository.findById(newParentId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "目标父租户不存在: " + newParentId));
        if (isAncestor(tenantId, newParentId)) {
            throw new BusinessException("1005", "不能将租户迁移到其自身或下级租户下");
        }

        // 阶段一：锁定。将租户置为 MIGRATING，迁移期间该租户及子树只读。
        // 乐观锁（@Version）：并发迁移同一租户时，后提交的事务会因版本号不匹配抛出
        // OptimisticLockException，从而避免两个迁移请求基于旧 path 各自更新导致 path 错乱。
        entity.setStatus(TenantStatus.MIGRATING);
        tenantInfoRepository.save(entity);

        String oldPath = entity.getPath();
        int oldLevel = entity.getLevel();
        Long oldParentId = entity.getParentId();

        // 先基于旧 path 查询子树（在修改自身 path 之前），避免查询条件依赖已经被改写的实体状态。
        // 此时子节点 path 尚未变更，故 findSubTree(oldPath) 仍可命中全部后代。
        List<TenantInfo> descendants = tenantInfoRepository.findSubTree(oldPath).stream()
                .filter(t -> !t.getId().equals(tenantId))
                .toList();

        String newPath = newParent.getPath() + tenantId + "/";
        int levelDelta = (newParent.getLevel() + 1) - oldLevel;

        entity.setParentId(newParentId);
        entity.setLevel(newParent.getLevel() + 1);
        entity.setPath(newPath);
        tenantInfoRepository.save(entity);

        // 阶段二：同步更新所有后代租户的路径与层级（子节点 path 尚未变更，可安全基于 oldPath 前缀替换）。
        // 迁移保证：本租户被锁定为 MIGRATING，外部对其及子树的写操作均被 assertNotMigrating 拒绝，故子树只读。
        for (TenantInfo child : descendants) {
            child.setPath(child.getPath().replace(oldPath, newPath));
            child.setLevel(child.getLevel() + levelDelta);
            tenantInfoRepository.save(child);
        }

        // 阶段三：解锁。迁移完成后恢复为启用状态。
        entity.setStatus(TenantStatus.ACTIVE);
        tenantInfoRepository.save(entity);

        TenantMigratedEvent migratedEvent = new TenantMigratedEvent();
        migratedEvent.setTenantId(tenantId);
        migratedEvent.setOldParentId(oldParentId);
        migratedEvent.setNewParentId(newParentId);
        eventPublisher.publish(migratedEvent);
    }

    private TenantInfo assertNotMigrating(Long tenantId) {
        TenantInfo entity = requireTenant(tenantId);
        if (entity.getStatus() == TenantStatus.MIGRATING) {
            throw new BusinessException("1006", "租户正在迁移中，当前为只读状态，禁止写操作: " + tenantId);
        }
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantDTO> getTenantAncestors(Long tenantId) {
        TenantInfo entity = requireTenant(tenantId);
        List<Long> ids = parsePath(entity.getPath());
        List<TenantInfo> ancestors = tenantInfoRepository.findAllById(ids);
        ancestors.sort(Comparator.comparing(TenantInfo::getPath));
        return tenantMapper.toDtoList(ancestors);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAncestor(Long ancestorTenantId, Long descendantTenantId) {
        // 语义：判断 ancestorTenantId 是否为 descendantTenantId 的祖先（即 descendant 是否位于 ancestor 的子树中）。
        // 因 path 采用 "/{id}/" 风格，若 descendant.path 包含 "/{ancestorId}/"，即表示 ancestor 在其祖先链上。
        // 注意：migrateTenant 中调用 isAncestor(tenantId, newParentId) 即用于判断 newParentId 是否为 tenantId 的后代，
        // 从而阻止“将租户迁移到自身或下级”的非法操作。
        TenantInfo descendant = requireTenant(descendantTenantId);
        return descendant.getPath().contains("/" + ancestorTenantId + "/");
    }

    private TenantTreeDTO buildChildren(TenantInfo node,
                                         Map<Long, List<TenantInfo>> childrenByParent) {
        TenantTreeDTO dto = new TenantTreeDTO();
        dto.setId(node.getId());
        dto.setTenantName(node.getTenantName());
        dto.setTenantType(node.getTenantType());
        dto.setStatus(node.getStatus());
        List<TenantInfo> children = childrenByParent.getOrDefault(node.getId(), List.of());
        dto.setChildren(children.stream()
                .sorted(Comparator.comparing(TenantInfo::getId))
                .map(c -> buildChildren(c, childrenByParent))
                .toList());
        return dto;
    }

    private TenantInfo requireTenant(Long tenantId) {
        return tenantInfoRepository.findById(tenantId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Tenant", tenantId));
    }

    private List<Long> parsePath(String path) {
        return java.util.Arrays.stream(path.split("/"))
                .filter(StringUtils::hasText)
                .map(Long::valueOf)
                .toList();
    }

    /** 合法的租户状态转换表：key 为源状态，value 为允许进入的目标状态集合 */
    private static final Map<TenantStatus, Set<TenantStatus>> ALLOWED_TRANSITIONS = Map.of(
            TenantStatus.ACTIVE, Set.of(TenantStatus.SUSPENDED, TenantStatus.LOCKED, TenantStatus.ARCHIVED),
            TenantStatus.SUSPENDED, Set.of(TenantStatus.ACTIVE, TenantStatus.LOCKED, TenantStatus.ARCHIVED),
            TenantStatus.LOCKED, Set.of(TenantStatus.ACTIVE, TenantStatus.SUSPENDED, TenantStatus.ARCHIVED),
            TenantStatus.MIGRATING, Set.of(TenantStatus.ACTIVE),
            TenantStatus.ARCHIVED, Set.of());

    private boolean isTransitionAllowed(TenantStatus oldStatus, TenantStatus newStatus) {
        if (oldStatus == newStatus) {
            return false;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(oldStatus, Set.of()).contains(newStatus);
    }
}
