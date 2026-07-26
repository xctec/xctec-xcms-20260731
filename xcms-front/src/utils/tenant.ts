/**
 * 登录前租户 ID 管理。
 *
 * 策略：URL 参数 ?tenantId=xxx > localStorage > 默认值。
 * - 启动时从 URL 解析 tenantId 并持久化（支持多租户通过链接切换）
 * - 登录请求从 getLoginTenantId() 取值带上
 * - 登录后 AuthState.tenantId 来自 LoginResult，用于后续请求的 X-Tenant-Id
 */
const STORAGE_KEY = 'xcms-login-tenant-id';
const DEFAULT_TENANT_ID = 1;

/** 获取登录用的租户 ID：localStorage > 默认值 */
export function getLoginTenantId(): number {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored && !Number.isNaN(Number(stored))) {
    return Number(stored);
  }
  return DEFAULT_TENANT_ID;
}

/**
 * 从 URL 参数 ?tenantId=xxx 读取并持久化到 localStorage。
 * 应用启动时调用一次；有则覆盖默认值，无则保持现状。
 */
export function initTenantIdFromUrl(): void {
  const params = new URLSearchParams(window.location.search);
  const fromUrl = params.get('tenantId');
  if (fromUrl && !Number.isNaN(Number(fromUrl))) {
    localStorage.setItem(STORAGE_KEY, fromUrl);
  }
}
