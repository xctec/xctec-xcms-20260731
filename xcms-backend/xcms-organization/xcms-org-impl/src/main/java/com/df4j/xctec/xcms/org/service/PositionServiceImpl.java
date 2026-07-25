package com.df4j.xctec.xcms.org.service;

import com.df4j.xctec.xcms.org.api.PositionService;
import com.df4j.xctec.xcms.org.api.dto.PositionCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.PositionDTO;
import com.df4j.xctec.xcms.org.api.dto.PositionUpdateRequest;
import com.df4j.xctec.xcms.org.domain.Position;
import com.df4j.xctec.xcms.org.mapper.PositionMapper;
import com.df4j.xctec.xcms.org.repository.PositionRepository;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    @Override
    @Transactional
    public PositionDTO createPosition(PositionCreateRequest request) {
        requireTenant();
        Position position = new Position();
        position.setDeptId(request.getDeptId());
        position.setPositionCode(request.getPositionCode());
        position.setPositionName(request.getPositionName());
        position.setLevel(request.getLevel());
        position.setSortOrder(request.getSortOrder());
        position.setStatus("ACTIVE");
        position.setCreatedBy(TenantContext.getCurrentUserId());
        return positionMapper.toDTO(positionRepository.save(position));
    }

    @Override
    @Transactional
    public PositionDTO updatePosition(Long positionId, PositionUpdateRequest request) {
        requireTenant();
        Position position = findPosition(positionId);
        positionMapper.updatePosition(position, request);
        position.setUpdatedBy(TenantContext.getCurrentUserId());
        return positionMapper.toDTO(positionRepository.save(position));
    }

    @Override
    @Transactional
    public void deletePosition(Long positionId) {
        requireTenant();
        Position position = findPosition(positionId);
        position.setDeletedAt(LocalDateTime.now());
        position.setUpdatedBy(TenantContext.getCurrentUserId());
        positionRepository.save(position);
    }

    @Override
    public List<PositionDTO> listPositionsByDept(Long deptId) {
        requireTenant();
        return positionMapper.toDTOList(positionRepository.findByDeptIdAndDeletedAtIsNull(deptId));
    }

    private Position findPosition(Long positionId) {
        return positionRepository.findByIdAndDeletedAtIsNull(positionId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.POSITION_NOT_FOUND, String.valueOf(positionId)));
    }

    private void requireTenant() {
        if (TenantContext.getTenantId() == null) {
            throw new BusinessException(ErrorCodes.TENANT_NOT_FOUND, "未确定租户上下文");
        }
    }
}
