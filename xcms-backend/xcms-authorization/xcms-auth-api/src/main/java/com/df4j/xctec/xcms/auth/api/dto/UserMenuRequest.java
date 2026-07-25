package com.df4j.xctec.xcms.auth.api.dto;

import lombok.Data;

/**
 * 按用户查询菜单请求体
 */
@Data
public class UserMenuRequest {
    private Long userId;
    private String scope;
}
