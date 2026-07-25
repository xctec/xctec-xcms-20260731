package com.df4j.xctec.xcms.kernel.context;

/**
 * 租户上下文，通过 ThreadLocal 在请求生命周期内传递租户与当前用户信息。
 * JPA @TenantId 通过 CurrentTenantIdentifierResolver 读取此上下文。
 */
public class TenantContext {

    private static final ThreadLocal<TenantInfo> CONTEXT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(Long tenantId, Long userId, String dataSourceKey) {
        CONTEXT.set(new TenantInfo(tenantId, userId, dataSourceKey));
    }

    public static void set(Long tenantId, Long userId) {
        set(tenantId, userId, "shared");
    }

    public static void set(Long tenantId) {
        set(tenantId, null, "shared");
    }

    public static Long getTenantId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.tenantId() : null;
    }

    public static Long getCurrentUserId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.userId() : null;
    }

    public static String getDataSourceKey() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.dataSourceKey() : "shared";
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /** 临时切换租户上下文（跨租户操作），返回原始上下文用于恢复 */
    public static TenantInfo switchTo(Long tenantId) {
        TenantInfo original = CONTEXT.get();
        set(tenantId, null, "shared");
        return original;
    }

    public static void restore(TenantInfo info) {
        if (info != null) {
            CONTEXT.set(info);
        } else {
            clear();
        }
    }

    public record TenantInfo(Long tenantId, Long userId, String dataSourceKey) {}
}
