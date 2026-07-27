package com.df4j.xctec.xcms.kernel.context;

/**
 * 租户上下文门面（过渡期薄包装，ADR-012）。
 *
 * <p>实际状态由 {@link ActorContext} 承载，本类仅做静态委托以兼容存量调用点
 * （135+ 处，分批迁移后删除本类）。新代码请直接使用 {@link ActorContext}。</p>
 *
 * <p>与旧版差异：{@code dataSourceKey} 已删除（全仓恒为 shared、无路由消费方）；
 * 三参 {@code set} 与 {@code getDataSourceKey()} 一并移除。</p>
 *
 * @deprecated 使用 {@link ActorContext}
 */
@Deprecated
public class TenantContext {

    private TenantContext() {
    }

    public static void set(Long tenantId, Long userId) {
        ActorContext.setUser(tenantId, userId);
    }

    public static void set(Long tenantId) {
        ActorContext.setUser(tenantId, null);
    }

    public static Long getTenantId() {
        return ActorContext.getTenantId();
    }

    public static Long getCurrentUserId() {
        return ActorContext.getCurrentUserId();
    }

    public static void clear() {
        ActorContext.clear();
    }

    /** 临时切换租户上下文（跨租户操作），返回原始上下文用于恢复 */
    public static TenantInfo switchTo(Long tenantId) {
        ActorContext.Actor original = ActorContext.switchTo(tenantId);
        return toTenantInfo(original);
    }

    public static void restore(TenantInfo info) {
        if (info != null) {
            ActorContext.set(info.tenantId(),
                    info.userId() == null ? null : ActorContext.Principal.user(info.userId()));
        } else {
            ActorContext.clear();
        }
    }

    private static TenantInfo toTenantInfo(ActorContext.Actor actor) {
        if (actor == null) {
            return null;
        }
        Long userId = actor.principal() != null ? actor.principal().userId() : null;
        return new TenantInfo(actor.tenantId(), userId);
    }

    /** 兼容存量 switchTo/restore 调用的快照载体（dataSourceKey 已移除） */
    public record TenantInfo(Long tenantId, Long userId) {
    }
}
