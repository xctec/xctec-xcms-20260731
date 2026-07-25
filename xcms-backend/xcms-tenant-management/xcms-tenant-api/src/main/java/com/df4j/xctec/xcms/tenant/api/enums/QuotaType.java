package com.df4j.xctec.xcms.tenant.api.enums;

/**
 * 配额类型
 */
public enum QuotaType {
    /** 用户数量 */
    USER_COUNT,
    /** 存储容量（字节） */
    STORAGE,
    /** API 调用次数 */
    API_CALL,
    /** 流程实例数 */
    PROCESS_INSTANCE
}
