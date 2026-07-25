package com.df4j.xctec.xcms.auth.api;

import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskDTO;
import com.df4j.xctec.xcms.auth.api.dto.ColumnMaskUpdateRequest;

import java.util.List;

public interface ColumnMaskService {

    ColumnMaskDTO createMaskRule(ColumnMaskCreateRequest request);

    ColumnMaskDTO updateMaskRule(Long ruleId, ColumnMaskUpdateRequest request);

    void deleteMaskRule(Long ruleId);

    List<ColumnMaskDTO> listMaskRules(String resourceType);
}
