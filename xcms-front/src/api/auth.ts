import { http } from './http';
import type { LoginRequest, LoginResult } from '@/types/identity';

// TODO(phase-0.2): 生成的 LoginResult 为 { token, refreshToken, expiresIn, user: UserDTO, forceChangePassword }
// （嵌套 user 对象），与前端的扁平 AuthState（userId/username/tenantId/tenantName/roles 在顶层）契约不一致。
// 此处沿用手写 LoginResult 以保证 Login.tsx / stores/auth.ts 可编译；后续需与后端对齐契约后再统一为 Unwrap 模式。
export const authApi = {
  login: (data: LoginRequest) =>
    http.post<unknown, LoginResult>('/api/auth/login', data),
  logout: () => http.post<unknown, void>('/api/auth/logout', {}),
  refreshToken: (refreshToken: string) =>
    http.post<unknown, LoginResult>('/api/auth/refresh', { refreshToken }),
  getUserInfo: () => http.post<unknown, LoginResult>('/api/auth/user-info', {}),
};
