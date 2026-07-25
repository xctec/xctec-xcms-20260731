package com.df4j.xctec.xcms.org.api;

import com.df4j.xctec.xcms.org.api.dto.PositionCreateRequest;
import com.df4j.xctec.xcms.org.api.dto.PositionDTO;
import com.df4j.xctec.xcms.org.api.dto.PositionUpdateRequest;

import java.util.List;

/**
 * 岗位服务
 */
public interface PositionService {

    PositionDTO createPosition(PositionCreateRequest request);

    PositionDTO updatePosition(Long positionId, PositionUpdateRequest request);

    void deletePosition(Long positionId);

    List<PositionDTO> listPositionsByDept(Long deptId);
}
