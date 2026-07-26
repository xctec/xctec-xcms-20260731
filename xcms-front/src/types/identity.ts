import type { Schemas, Unwrap } from '@/types/api-helpers';

export interface UserDTO {
  id: number;
  username: string;
  realName: string;
  email: string | null;
  phone: string | null;
  avatar: string | null;
  status: string;
  gender: string | null;
  userType: string;
  remark: string | null;
  createdAt: string;
}

export interface UserBriefDTO {
  id: number;
  username: string;
  realName: string;
  avatar: string | null;
}

export interface RoleDTO {
  id: number;
  roleCode: string;
  roleName: string;
  description: string | null;
  scopeType: string;
  scopeValue: string | null;
  status: string;
}

/**
 * 登录请求：重导出后端生成类型，作为契约单一来源。
 * 生成的 LoginRequest 额外含 tenantId / deviceType（多租户登录场景），此处统一复用。
 */
export type LoginRequest = Schemas['LoginRequest'];

/**
 * 登录结果：重导出后端生成类型 `ApiResponseLoginResult['data']`（嵌套 user）。
 *
 * 真实后端结构为 `{ token, refreshToken, expiresIn, user: UserDTO, forceChangePassword }`
 * （user 内嵌），与前端的扁平 AuthState 不同，由 stores/auth.ts 的 setAuth 负责解平。
 */
export type LoginResult = Unwrap<Schemas['ApiResponseLoginResult']>;
