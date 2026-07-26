package com.df4j.xctec.xcms.auth.service;

import com.df4j.xctec.xcms.auth.api.CrossTenantResourceService;
import com.df4j.xctec.xcms.auth.api.dto.CrossTenantResourceDTO;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceQuery;
import com.df4j.xctec.xcms.auth.api.dto.request.CrossTenantResourceUpdateRequest;
import com.df4j.xctec.xcms.auth.domain.CrossTenantResource;
import com.df4j.xctec.xcms.auth.repository.CrossTenantResourceRepository;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
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
public class CrossTenantResourceServiceImpl implements CrossTenantResourceService {

    private final CrossTenantResourceRepository resourceRepository;

    @Override
    @Transactional
    public CrossTenantResourceDTO create(CrossTenantResourceCreateRequest request) {
        if (resourceRepository.existsByTenantIdAndResourceKey(request.getTenantId(), request.getResourceKey())) {
            throw new BusinessException(ErrorCodes.ALREADY_EXISTS, "资源标识已存在: " + request.getResourceKey());
        }
        CrossTenantResource r = new CrossTenantResource();
        r.setTenantId(request.getTenantId());
        r.setResourceType(request.getResourceType());
        r.setResourceKey(request.getResourceKey());
        r.setResourceName(request.getResourceName());
        r.setDescription(request.getDescription());
        r.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        return toDto(resourceRepository.save(r));
    }

    @Override
    @Transactional
    public CrossTenantResourceDTO update(CrossTenantResourceUpdateRequest request) {
        CrossTenantResource r = getOrThrow(request.getId());
        if (request.getResourceType() != null) {
            r.setResourceType(request.getResourceType());
        }
        if (request.getResourceKey() != null) {
            r.setResourceKey(request.getResourceKey());
        }
        if (request.getResourceName() != null) {
            r.setResourceName(request.getResourceName());
        }
        if (request.getDescription() != null) {
            r.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            r.setStatus(request.getStatus());
        }
        return toDto(resourceRepository.save(r));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        resourceRepository.deleteById(id);
    }

    @Override
    public CrossTenantResourceDTO get(Long id) {
        return toDto(getOrThrow(id));
    }

    @Override
    public PageResult<CrossTenantResourceDTO> list(CrossTenantResourceQuery query) {
        Specification<CrossTenantResource> spec = (root, cq, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (query.getTenantId() != null) {
                predicates.add(cb.equal(root.get("tenantId"), query.getTenantId()));
            }
            if (StringUtils.hasText(query.getResourceType())) {
                predicates.add(cb.equal(root.get("resourceType"), query.getResourceType()));
            }
            if (StringUtils.hasText(query.getStatus())) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<CrossTenantResource> page = resourceRepository.findAll(spec,
                PageRequest.of(query.getPage() - 1, query.getSize()));
        return PageResult.of(page.getContent().stream().map(this::toDto).toList(), page.getTotalElements());
    }

    private CrossTenantResource getOrThrow(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "资源不存在: " + id));
    }

    private CrossTenantResourceDTO toDto(CrossTenantResource r) {
        CrossTenantResourceDTO dto = new CrossTenantResourceDTO();
        dto.setId(r.getId());
        dto.setTenantId(r.getTenantId());
        dto.setResourceType(r.getResourceType());
        dto.setResourceKey(r.getResourceKey());
        dto.setResourceName(r.getResourceName());
        dto.setDescription(r.getDescription());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
