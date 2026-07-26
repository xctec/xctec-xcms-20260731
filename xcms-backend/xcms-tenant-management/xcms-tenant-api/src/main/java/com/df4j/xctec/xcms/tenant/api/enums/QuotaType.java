package com.df4j.xctec.xcms.tenant.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** 配额类型。 */
@Schema(description = "配额类型：USER_COUNT-用户数量，STORAGE-存储容量(字节)，API_CALL-API调用次数，PROCESS_INSTANCE-流程实例数")
public enum QuotaType {
    /** 用户数量 */
    @Schema(description = "用户数量：租户允许的最大用户数")
    USER_COUNT,
    /** 存储容量（字节） */
    @Schema(description = "存储容量：租户允许的最大存储容量（字节）")
    STORAGE,
    /** API 调用次数 */
    @Schema(description = "API 调用次数：租户允许的 API 调用配额")
    API_CALL,
    /** 流程实例数 */
    @Schema(description = "流程实例数：租户允许的最大流程实例数")
    PROCESS_INSTANCE
}
