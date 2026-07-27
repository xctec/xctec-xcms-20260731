import { authApi } from './auth';
import { useAuthStore } from '@/stores/auth';

/**
 * 令牌刷新管理器。
 * 并发的 401 只会触发一次刷新请求，其余调用排队等待同一结果，
 * 避免「刷新风暴」。刷新成功后由调用方重试原始请求。
 */

let refreshPromise: Promise<boolean> | null = null;

/** 执行一次刷新（内部，带并发去重）。refresh token 在 httpOnly cookie 中由浏览器携带（AT-12） */
function doRefresh(): Promise<boolean> {
  const { setTokens, clearAuth } = useAuthStore.getState();
  return authApi
    .refreshToken()
    .then((res) => {
      if (!res.token) {
        clearAuth();
        return false;
      }
      setTokens({ token: res.token, expiresIn: res.expiresIn });
      return true;
    })
    .catch(() => {
      clearAuth();
      return false;
    });
}

/** 获取一个刷新 Promise（并发安全） */
export function refreshTokens(): Promise<boolean> {
  if (!refreshPromise) {
    refreshPromise = doRefresh().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}
