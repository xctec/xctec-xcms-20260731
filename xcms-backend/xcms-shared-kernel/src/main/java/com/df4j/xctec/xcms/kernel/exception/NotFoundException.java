package com.df4j.xctec.xcms.kernel.exception;

/**
 * 资源未找到异常。
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String resource, Long id) {
        super(ErrorCodes.NOT_FOUND, resource + " not found: " + id);
    }

    public NotFoundException(String resource, String key) {
        super(ErrorCodes.NOT_FOUND, resource + " not found: " + key);
    }
}
