import axios, {
  type AxiosError,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios';
import type { ApiResponse } from '@/types/common';
import { useAuthStore } from '@/stores/auth';
import { refreshTokens } from './tokenRefresh';

const REFRESH_URL = '/api/auth/refresh';
const LOGIN_URL = '/api/auth/login';

type RetryableConfig = InternalAxiosRequestConfig & { _retry?: boolean };

const http = axios.create({
  baseURL: '/',
  timeout: 30_000,
  headers: { 'Content-Type': 'application/json' },
});

// 请求拦截器：注入令牌 / 租户，并在访问令牌即将过期时主动刷新
// 并发 401 仅跳转一次登录页
let isRedirecting = false;
function redirectToLogin() {
  if (isRedirecting) return;
  isRedirecting = true;
  useAuthStore.getState().clearAuth();
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}

http.interceptors.request.use(async (config) => {
  const state = useAuthStore.getState();
  if (
    state.token &&
    config.url !== REFRESH_URL &&
    config.url !== LOGIN_URL &&
    state.isAboutToExpire()
  ) {
    // 主动刷新失败：中断请求，避免携带空 token 发出并跳转登录
    const ok = await refreshTokens();
    if (!ok) {
      redirectToLogin();
      return Promise.reject(new Error('token refresh failed'));
    }
  }
  const { token, tenantId } = useAuthStore.getState();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  if (tenantId) config.headers['X-Tenant-Id'] = String(tenantId);
  return config;
});

// 响应拦截器：解包 data；401 触发刷新并重试，刷新失败则清除登录态跳登录
http.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<unknown>>) => {
    const body = response.data;
    if (body.errorCode !== '0') {
      return Promise.reject(new Error(body.errorMsg));
    }
    // 解包：返回 data 字段
    return body.data as never;
  },
  async (error: AxiosError) => {
    const status = error.response?.status;
    const config = error.config as RetryableConfig | undefined;

    const canRetry =
      status === 401 &&
      config &&
      !config._retry &&
      config.url !== REFRESH_URL &&
      config.url !== LOGIN_URL;

    if (canRetry) {
      config._retry = true;
      const ok = await refreshTokens();
      if (ok) {
        const token = useAuthStore.getState().token;
        if (token) config.headers.Authorization = `Bearer ${token}`;
        return http(config);
      }
    }

    if (status === 401 && config?.url !== LOGIN_URL) {
      redirectToLogin();
    }
    return Promise.reject(error);
  }
);

export { http };
