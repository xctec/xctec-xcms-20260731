package com.df4j.xctec.xcms.identity.api.dto;

import lombok.Data;

/**
 * 更新用户请求
 */
@Data
public class UserUpdateRequest {
    /** 用户 ID（全 POST 风格，由请求体携带，替代 @PathVariable） */
    private Long id;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
}
