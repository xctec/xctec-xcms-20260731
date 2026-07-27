package com.df4j.xctec.xcms.kernel.context;

/**
 * 调用者身份上下文（ADR-012）。
 *
 * <p>原 {@code TenantContext} 实际承载的是「本次调用的执行者身份」：所属租户 + 主体
 * （真实用户或系统服务）。更名为 ActorContext 以名副其实，并将主体抽象为
 * {@link Principal}：用户态请求为 userId，系统触发场景（调度/MQ 消费/服务间调用）
 * 为 service principal（如 {@code system@task-scheduler}），不再以 null 表达"系统"。</p>
 *
 * <p>{@code dataSourceKey} 已删除：全仓恒为 shared、无任何路由消费方，属死字段。
 * 未来分库时由租户元数据（tenant_info.datasource_key）驱动路由，不经线程上下文。</p>
 *
 * <p>{@link TenantContext} 暂保留为本类的薄包装，存量调用点分批迁移后移除。</p>
 */
public final class ActorContext {

    private static final ThreadLocal<Actor> CONTEXT = new ThreadLocal<>();

    private ActorContext() {
    }

    /** 调用主体：真实用户（userId）或系统服务（serviceName），二者互斥 */
    public record Principal(Long userId, String serviceName) {

        public static Principal user(Long userId) {
            return new Principal(userId, null);
        }

        public static Principal service(String serviceName) {
            return new Principal(null, serviceName);
        }

        public boolean isService() {
            return serviceName != null;
        }
    }

    /** 执行者：所属租户 + 主体 */
    public record Actor(Long tenantId, Principal principal) {
    }

    public static void set(Long tenantId, Principal principal) {
        CONTEXT.set(new Actor(tenantId, principal));
    }

    /** 用户态请求 */
    public static void setUser(Long tenantId, Long userId) {
        set(tenantId, userId == null ? null : Principal.user(userId));
    }

    /** 系统触发场景（调度/MQ/服务间调用），principal 为服务主体而非 null */
    public static void setService(Long tenantId, String serviceName) {
        set(tenantId, Principal.service(serviceName));
    }

    public static Actor current() {
        return CONTEXT.get();
    }

    public static Long getTenantId() {
        Actor actor = CONTEXT.get();
        return actor != null ? actor.tenantId() : null;
    }

    public static Principal getPrincipal() {
        Actor actor = CONTEXT.get();
        return actor != null ? actor.principal() : null;
    }

    /** 用户主体的 userId；服务主体或未认证时为 null */
    public static Long getCurrentUserId() {
        Principal principal = getPrincipal();
        return principal != null ? principal.userId() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 临时切换租户（跨租户操作），保留当前主体身份（审计可追溯操作人），
     * 返回原始上下文用于恢复。
     */
    public static Actor switchTo(Long tenantId) {
        Actor original = CONTEXT.get();
        Principal principal = original != null ? original.principal() : null;
        CONTEXT.set(new Actor(tenantId, principal));
        return original;
    }

    public static void restore(Actor actor) {
        if (actor != null) {
            CONTEXT.set(actor);
        } else {
            clear();
        }
    }
}
