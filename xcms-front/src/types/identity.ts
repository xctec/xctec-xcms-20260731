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

/** 登录请求：直接复用生成类型，作为契约单一来源 */
export type LoginRequest = Schemas['LoginRequest'];

/** 后端登录结果（生成类型，契约单一来源）；前端扁平 LoginResult 由其映射而来（见 api/auth.ts） */
export type LoginResultDTO = Unwrap<Schemas['ApiResponseLoginResult']>;

export interface LoginResult {
  token: string;
  refreshToken: string;
  userId: number;
  username: string;
  realName: string;
  tenantId: number;
  tenantName: string;
  roles: string[];
  expiresIn: number;
}
