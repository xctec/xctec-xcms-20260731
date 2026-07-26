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

export interface LoginRequest {
  username: string;
  password: string;
}

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
