package com.df4j.xctec.xcms.tenant.api;

import com.df4j.xctec.xcms.tenant.api.dto.TenantInitStatusDTO;

import java.util.List;

/**
 * 租户初始化状态跟踪与补偿（ADR-015）。
 *
 * <p>租户创建走事件驱动初始化（identity 默认角色/管理员、org 根部门），事件消费失败时
 * 初始化不完整会产生"半初始化"租户。各初始化监听器通过 {@link #record} 上报结果，
 * 管理面可查询失败记录并触发 {@link #retry} 补偿（重发 TenantCreatedEvent，
 * 监听器幂等保证不重复初始化）。</p>
 */
public interface TenantInitStatusService {

    /** 初始化成功状态 */
    String STATUS_SUCCESS = "SUCCESS";
    /** 初始化失败状态 */
    String STATUS_FAILED = "FAILED";

    /** identity 模块（默认角色/管理员） */
    String MODULE_IDENTITY = "identity";
    /** org 模块（根部门） */
    String MODULE_ORG = "org";

    /**
     * 记录某租户某模块的初始化结果（独立事务，失败链路中也能落库）。
     *
     * @param tenantId 租户 ID
     * @param module   模块标识，见 MODULE_* 常量
     * @param success  是否成功
     * @param errorMsg 失败原因（成功时传 null）
     */
    void record(Long tenantId, String module, boolean success, String errorMsg);

    /**
     * 查询所有初始化失败的记录。
     */
    List<TenantInitStatusDTO> listFailed();

    /**
     * 对指定租户触发初始化补偿：重发 TenantCreatedEvent，由幂等监听器补齐缺失数据。
     *
     * @param tenantId 租户 ID
     */
    void retry(Long tenantId);
}
