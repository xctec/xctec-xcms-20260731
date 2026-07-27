import { http } from './http';
import type { LoginRequest, LoginResult } from '@/types/identity';

export const authApi = {
  login: (data: LoginRequest) =>
    http.post<unknown, LoginResult>('/api/auth/login', data),
  logout: () => http.post<unknown, void>('/api/auth/logout', {}),
  // AT-12：refresh token 在 httpOnly cookie 中，由浏览器自动携带，不再经 body 传递
  refreshToken: () => http.post<unknown, LoginResult>('/api/auth/refresh', {}),
  getUserInfo: () => http.post<unknown, LoginResult>('/api/auth/user-info', {}),
};
