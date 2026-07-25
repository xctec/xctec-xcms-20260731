package com.df4j.xctec.xcms.kernel.exception;

/**
 * 系统通用错误码（字符串数字，兼容更多系统）。
 * <p>
 * 编码规则：
 * - "0"           成功
 * - "400" ~ "499" 客户端错误（参照 HTTP 语义）
 * - "500" ~ "599" 服务端错误
 * - "1xxx"        业务错误（按模块分段）
 *   - 10xx 租户相关
 *   - 11xx 用户/组织相关
 *   - 12xx 权限相关
 *   - 13xx 流程相关
 *   - 14xx 消息/文件/任务相关
 */
public final class ErrorCodes {

    private ErrorCodes() {}

    /** 成功 */
    public static final String SUCCESS = "0";

    /** 客户端错误 */
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String PERMISSION_DENIED = "403";
    public static final String NOT_FOUND = "404";
    public static final String ALREADY_EXISTS = "409";
    public static final String VALIDATION_ERROR = "422";

    /** 服务端错误 */
    public static final String INTERNAL_ERROR = "500";

    /** 租户相关 10xx */
    public static final String TENANT_NOT_FOUND = "1001";
    public static final String TENANT_SUSPENDED = "1002";
    public static final String TENANT_LOCKED = "1003";
    public static final String QUOTA_EXCEEDED = "1004";

    /** 用户/组织相关 11xx */
    public static final String USER_NOT_FOUND = "1101";
    public static final String USER_ALREADY_EXISTS = "1102";
    public static final String USER_DISABLED = "1103";
    public static final String ROLE_NOT_FOUND = "1104";
    public static final String ROLE_ALREADY_EXISTS = "1105";
    public static final String DEPT_NOT_FOUND = "1106";
    public static final String DEPT_ALREADY_EXISTS = "1107";
    public static final String POSITION_NOT_FOUND = "1108";
    public static final String GROUP_NOT_FOUND = "1109";

    /** 认证相关 12xx */
    public static final String AUTH_INVALID_CREDENTIALS = "1203";
    public static final String AUTH_TOKEN_INVALID = "1204";
    public static final String AUTH_TOKEN_EXPIRED = "1205";
    public static final String AUTH_ACCOUNT_DISABLED = "1206";

    /** 权限相关 12xx */
    public static final String DATA_PERMISSION_DENIED = "1202";

    /** 通用业务错误 */
    public static final String BUSINESS_ERROR = "4000";

    /** 跨租户相关 20xx */
    public static final String CROSS_TENANT_DENIED = "2001";
    public static final String BUSINESS_VISIBILITY_DENIED = "2002";
}
