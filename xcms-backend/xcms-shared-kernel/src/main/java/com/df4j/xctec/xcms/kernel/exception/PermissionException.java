package com.df4j.xctec.xcms.kernel.exception;

/**
 * 权限异常。
 */
public class PermissionException extends BusinessException {

    public PermissionException(String message) {
        super(ErrorCodes.PERMISSION_DENIED, message);
    }
}
