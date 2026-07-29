import { http } from './http';
import type { LoginRequest, LoginResult } from '@/types/identity';
import type { UserDTO } from '@/types/user';

export interface RegisterRequest {
  username: string;
  password: string;
  confirmPassword?: string;
  name?: string;
  email?: string;
  phone?: string;
  tenantCode?: string;
}

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

  // 以下端点尚未在后端 openapi 中提供，前端先补齐调用以完成页面对接，待后端补充对应接口
  register: (data: RegisterRequest) => http.post<unknown, void>('/api/auth/register', data),
  forgotPassword: (data: { email: string }) =>
    http.post<unknown, void>('/api/auth/forgot-password', data),
  resetPassword: (data: { token: string; newPassword: string }) =>
    http.post<unknown, void>('/api/auth/reset-password', data),
  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    http.post<unknown, void>('/api/auth/change-password', data),
  updateProfile: (data: Partial<UserDTO>) =>
    http.post<unknown, void>('/api/auth/profile', data),
};

export default authApi;
