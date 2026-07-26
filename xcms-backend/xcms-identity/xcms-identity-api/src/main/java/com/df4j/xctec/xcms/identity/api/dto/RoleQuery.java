package com.df4j.xctec.xcms.identity.api.dto;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQuery extends PageQuery {
    @Schema(description = "关键字（角色名/编码模糊匹配）")
    private String keyword;
    @Schema(description = "角色类型")
    private String roleType;
}
