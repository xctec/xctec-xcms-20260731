package com.df4j.xctec.xcms.kernel.exception;

import lombok.Getter;

/**
 * 业务异常基类。
 * errorCode 用 String 类型，字符串数字，兼容更多系统。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message) {
        this(ErrorCodes.INTERNAL_ERROR, message);
    }
}
