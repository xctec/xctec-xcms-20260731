package com.df4j.xctec.xcms.auth.api;

import com.df4j.xctec.xcms.auth.api.dto.DataRuleCreateRequest;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleDTO;
import com.df4j.xctec.xcms.auth.api.dto.DataRuleUpdateRequest;
import com.df4j.xctec.xcms.datapermission.api.DataPermissionContext;

import java.util.List;

public interface DataRuleService {

    DataRuleDTO createRule(DataRuleCreateRequest request);

    DataRuleDTO updateRule(Long ruleId, DataRuleUpdateRequest request);

    void deleteRule(Long ruleId);

    List<DataRuleDTO> listRules(String resourceType);

    void bindRuleToRole(Long ruleId, Long roleId, String scopeValue);

    void unbindRuleFromRole(Long ruleId, Long roleId);

    DataPermissionContext testPermission(Long userId, String resourceType);
}
