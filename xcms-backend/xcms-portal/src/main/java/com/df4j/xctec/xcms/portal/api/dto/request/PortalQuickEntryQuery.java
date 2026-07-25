package com.df4j.xctec.xcms.portal.api.dto.request;

import lombok.Data;

/**
 * 查询用户快捷入口请求。
 */
@Data
public class PortalQuickEntryQuery {

    /** 门户面：ADMIN / BUSINESS */
    private String surface;
}
