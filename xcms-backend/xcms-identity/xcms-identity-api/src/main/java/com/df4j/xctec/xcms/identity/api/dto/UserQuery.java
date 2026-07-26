package com.df4j.xctec.xcms.identity.api.dto;

import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.kernel.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {
    @Schema(description = "关键字（用户名/姓名模糊匹配）")
    private String keyword;
    @Schema(description = "部门ID")
    private Long deptId;
    @Schema(description = "用户状态")
    private UserStatus status;
}
