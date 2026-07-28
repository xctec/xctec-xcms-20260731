import { http } from './http';
import type { LoginRequest, LoginResult } from '@/types/identity';

export const authApi = {
  login: (data: LoginRequest) =>
    http.post<unknown, LoginResult>('/api/auth/login', data),
  logout: () => http.post<unknown, void>('/api/auth/logout', {}),
  // AT-12：refresh token 在 httpOnly cookie 中，由浏览器自动携带，不再经 body 传递；
  // X-Requested-With 为后端 CSRF 双保险校验所需（评审 P1-4）
  refreshToken: () =>
    http.post<unknown, LoginResult>('/api/auth/refresh', {}, {
      headers: { 'X-Requested-With': 'XMLHttpRequest' },
    }),
  getUserInfo: () => http.post<unknown, LoginResult>('/api/auth/user-info', {}),
};
