package com.df4j.xctec.xcms.kernel.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 通用成员变更请求体（id + 成员 ID 列表）。
 */
@Data
public class MembersRequest {
    private Long id;
    private List<Long> memberIds;
}
