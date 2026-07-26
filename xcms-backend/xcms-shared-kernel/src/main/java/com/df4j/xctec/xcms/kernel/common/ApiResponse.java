package com.df4j.xctec.xcms.kernel.common;

import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应包装。
 * errorCode 用 String 类型（字符串数字），兼容更多系统。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    @Schema(
        description = "错误码。\"0\"=成功；其他为业务/系统错误码：4xx 客户端错误（400 参数错误/401 未认证/403 无权限/404 资源不存在/409 资源冲突/422 参数校验失败）；5xx 服务端错误（500 内部错误）；1xxx 业务错误——100x 租户（1001 租户不存在/1002 租户停用/1003 租户锁定/1004 配额超限）、11xx 用户组织（1101 用户不存在/1102 用户已存在/1103 用户禁用/1104 角色不存在/1105 角色已存在/1106 部门不存在/1107 部门已存在/1108 岗位不存在/1109 用户组不存在）；12xx 认证权限（1202 数据权限拒绝/1203 凭证无效/1204 Token无效/1205 Token过期/1206 账号禁用）；4000 通用业务错误；20xx 跨租户（2001 跨租户拒绝/2002 业务可见性拒绝）",
        example = "0"
    )
    private String errorCode;

    @Schema(description = "错误信息，成功时为 \"success\"", example = "success")
    private String errorMsg;

    @Schema(description = "响应数据（泛型，可为 null）")
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCodes.SUCCESS, "success", data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ErrorCodes.SUCCESS, "success", null);
    }

    public static <T> ApiResponse<T> error(String errorCode, String errorMsg) {
        return new ApiResponse<>(errorCode, errorMsg, null);
    }
}
