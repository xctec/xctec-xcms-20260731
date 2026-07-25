package com.df4j.xctec.xcms.portal.api.dto;

import lombok.Data;

/**
 * 用户自定义快捷入口。
 */
@Data
public class PortalQuickEntryDTO {

    private Long id;

    /** 门户面：ADMIN / BUSINESS */
    private String surface;

    private String title;
    private String icon;
    private String url;

    /** 打开方式：SELF / BLANK */
    private String target;

    private Integer sortOrder;

    private Boolean enabled;
}
