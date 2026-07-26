import { http } from './http';
import type { LoginRequest, LoginResult, LoginResultDTO } from '@/types/identity';

/**
 * 后端 LoginResult 为嵌套结构 { token, refreshToken, expiresIn, user: UserDTO, forceChangePassword }，
 * 前端 AuthState 采用扁平结构。以生成类型 LoginResultDTO 为契约单一来源，
 * 此处显式映射为前端扁平 LoginResult（AuthState 改造见 Phase 0.3）。
 */
const toLoginResult = (dto: LoginResultDTO): LoginResult => ({
  token: dto.token ?? '',
  refreshToken: dto.refreshToken ?? '',
  expiresIn: dto.expiresIn ?? 0,
  userId: dto.user?.id ?? 0,
  username: dto.user?.username ?? '',
  realName: dto.user?.realName ?? '',
  tenantId: dto.user?.tenantId ?? 0,
  tenantName: (dto.user as unknown as { tenantName?: string } | undefined)?.tenantName ?? '',
  roles: dto.user?.roles?.map((r) => r.roleCode ?? '') ?? [],
});

export const authApi = {
  login: (data: LoginRequest) =>
    http.post<unknown, LoginResultDTO>('/api/auth/login', data).then(toLoginResult),
  logout: () => http.post<unknown, void>('/api/auth/logout', {}),
  refreshToken: (refreshToken: string) =>
    http.post<unknown, LoginResultDTO>('/api/auth/refresh', { refreshToken }).then(toLoginResult),
  getUserInfo: () =>
    http.post<unknown, LoginResultDTO>('/api/auth/user-info', {}).then(toLoginResult),
};
