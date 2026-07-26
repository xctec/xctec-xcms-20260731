import axios, { type AxiosResponse } from 'axios';
import type { ApiResponse } from '@/types/common';
import { useAuthStore } from '@/stores/auth';

const http = axios.create({
  baseURL: '/',
  timeout: 30_000,
  headers: { 'Content-Type': 'application/json' },
});

// 请求拦截器
http.interceptors.request.use((config) => {
  const { token, tenantId } = useAuthStore.getState();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  if (tenantId) config.headers['X-Tenant-Id'] = String(tenantId);
  return config;
});

// 响应拦截器
http.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<unknown>>) => {
    const body = response.data;
    if (body.errorCode !== '0') {
      return Promise.reject(new Error(body.errorMsg));
    }
    // 解包：返回 data 字段
    return body.data as never;
  },
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().clearAuth();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export { http };
