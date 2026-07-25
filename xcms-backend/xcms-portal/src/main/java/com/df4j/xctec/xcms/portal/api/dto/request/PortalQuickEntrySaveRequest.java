package com.df4j.xctec.xcms.portal.api.dto.request;

import com.df4j.xctec.xcms.portal.api.dto.PortalQuickEntryDTO;
import lombok.Data;

import java.util.List;

/**
 * 保存（覆盖式）用户快捷入口请求。
 */
@Data
public class PortalQuickEntrySaveRequest {

    /** 门户面：ADMIN / BUSINESS */
    private String surface;

    private List<PortalQuickEntryDTO> entries;
}
