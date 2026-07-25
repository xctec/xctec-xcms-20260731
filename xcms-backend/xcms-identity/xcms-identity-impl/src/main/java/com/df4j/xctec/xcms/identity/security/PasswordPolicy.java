package com.df4j.xctec.xcms.identity.security;

import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 密码强度策略校验。
 *
 * <p>规则（可通过配置覆盖）：最小长度 8，且同时包含大写字母、小写字母、数字与特殊字符。
 * 在创建用户、修改密码、管理员重置密码时调用 {@link #validate(String)} 进行校验。</p>
 */
@Component
public class PasswordPolicy {

    @Value("${xcms.identity.password.min-length:8}")
    private int minLength;

    @Value("${xcms.identity.password.require-uppercase:true}")
    private boolean requireUppercase;

    @Value("${xcms.identity.password.require-lowercase:true}")
    private boolean requireLowercase;

    @Value("${xcms.identity.password.require-digit:true}")
    private boolean requireDigit;

    @Value("${xcms.identity.password.require-special:true}")
    private boolean requireSpecial;

    public void validate(String password) {
        if (password == null || password.length() < minLength) {
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "密码长度至少 " + minLength + " 位");
        }
        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "密码必须包含大写字母");
        }
        if (requireLowercase && !password.matches(".*[a-z].*")) {
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "密码必须包含小写字母");
        }
        if (requireDigit && !password.matches(".*\\d.*")) {
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "密码必须包含数字");
        }
        if (requireSpecial && !password.matches(".*[^A-Za-z0-9].*")) {
            throw new BusinessException(ErrorCodes.BUSINESS_ERROR, "密码必须包含特殊字符");
        }
    }
}
