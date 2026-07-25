package com.df4j.xctec.xcms.org.api.dto;

import com.df4j.xctec.xcms.kernel.common.PageQuery;
import lombok.Data;

/**
 * 查询部门成员请求体
 */
@Data
public class DepartmentUsersRequest {
    private Long deptId;
    private PageQuery page;
}
