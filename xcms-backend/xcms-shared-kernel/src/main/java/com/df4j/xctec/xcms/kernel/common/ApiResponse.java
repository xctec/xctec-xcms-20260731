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

    @Schema(description = "错误码，\"0\" 表示成功，其他为业务错误码（如 \"1001\"）")
    private String errorCode;

    @Schema(description = "错误信息，成功时为 \"success\"")
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
