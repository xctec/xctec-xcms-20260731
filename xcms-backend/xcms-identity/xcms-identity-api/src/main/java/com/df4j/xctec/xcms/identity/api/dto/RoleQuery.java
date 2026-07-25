package com.df4j.xctec.xcms.identity.api.dto;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQuery extends PageQuery {
    private String keyword;
    private String roleType;
}
