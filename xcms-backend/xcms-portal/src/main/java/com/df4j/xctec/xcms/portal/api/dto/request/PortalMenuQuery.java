package com.df4j.xctec.xcms.portal.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 按门户面查询菜单请求。
 */
@Data
public class PortalMenuQuery {

    /** 门户面：ADMIN / BUSINESS */
    @Schema(description = "门户面（ADMIN/BUSINESS）")
    private String scope;
}
