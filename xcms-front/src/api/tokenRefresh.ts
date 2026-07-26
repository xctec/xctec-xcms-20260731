import { authApi } from './auth';
import { useAuthStore } from '@/stores/auth';

/**
 * 令牌刷新管理器。
 * 并发的 401 只会触发一次刷新请求，其余调用排队等待同一结果，
 * 避免「刷新风暴」。刷新成功后由调用方重试原始请求。
 */

let refreshPromise: Promise<boolean> | null = null;

/** 执行一次刷新（内部，带并发去重） */
function doRefresh(): Promise<boolean> {
  const { refreshToken, setTokens, clearAuth } = useAuthStore.getState();
  if (!refreshToken) {
    clearAuth();
    return Promise.resolve(false);
  }
  return authApi
    .refreshToken(refreshToken)
    .then((res) => {
      setTokens({ token: res.token, refreshToken: res.refreshToken, expiresIn: res.expiresIn });
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
