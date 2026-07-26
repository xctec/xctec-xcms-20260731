package com.df4j.xctec.xcms.identity.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256-GCM 加解密工具，用于加密存储敏感配置（如 SSO client_secret）。
 * <p>
 * 密钥来源优先级：环境变量 {@code XCMS_SSO_SECRET_KEY} &gt; 系统属性 {@code xcms.sso.secret-key} &gt; 内置默认值。
 * 生产环境务必通过环境变量注入密钥。密文以 {@code enc:} 前缀标记，便于幂等加密与兼容历史明文数据。
 */
public final class CryptoUtil {

    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static final String PREFIX = "enc:";
    private static final SecretKeySpec KEY = deriveKey();
    private static final SecureRandom RANDOM = new SecureRandom();

    private CryptoUtil() {
    }

    private static SecretKeySpec deriveKey() {
        String material = System.getenv("XCMS_SSO_SECRET_KEY");
        if (material == null || material.isBlank()) {
            material = System.getProperty("xcms.sso.secret-key");
        }
        if (material == null || material.isBlank()) {
            material = "xcms-default-sso-secret-key-change-me";
        }
        try {
            byte[] key = MessageDigest.getInstance("SHA-256")
                    .digest(material.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("初始化加密密钥失败", e);
        }
    }

    /**
     * 加密。null/空串原样返回；已加密（enc: 前缀）原样返回，保证幂等。
     */
    public static String encrypt(String plain) {
        if (plain == null || plain.isEmpty() || plain.startsWith(PREFIX)) {
            return plain;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("加密失败", e);
        }
    }

    /**
     * 解密。null/空串或非 enc: 前缀（历史明文）原样返回。
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty() || !cipherText.startsWith(PREFIX)) {
            return cipherText;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText.substring(PREFIX.length()));
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            byte[] data = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("解密失败", e);
        }
    }
}
