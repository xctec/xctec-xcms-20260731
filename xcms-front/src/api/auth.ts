import { http } from './http';
import type { LoginRequest, LoginResult } from '@/types/identity';

export const authApi = {
  login: (data: LoginRequest) =>
    http.post<unknown, LoginResult>('/api/auth/login', data),
  logout: () => http.post<unknown, void>('/api/auth/logout', {}),
  refreshToken: (refreshToken: string) =>
    http.post<unknown, LoginResult>('/api/auth/refresh', { refreshToken }),
  getUserInfo: () => http.post<unknown, LoginResult>('/api/auth/user-info', {}),
};
