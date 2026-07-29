import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const userApi = {
  list: (params: Schemas['UserQuery']) =>
    http.post<unknown, Unwrap<Schemas['ApiResponsePageResultUserDTO']>>('/api/admin/user/list', params),
  get: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseUserDTO']>>('/api/admin/user/get', { id }),
  create: (data: Schemas['UserCreateRequest']) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseUserDTO']>>('/api/admin/user/create', data),
  update: (data: Schemas['UserUpdateRequest']) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/user/update', data),
  delete: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/user/delete', { id }),
  resetPassword: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/user/reset-password', { id }),
};

export const roleApi = {
  list: (params?: Schemas['RoleQuery']) =>
    http.post<unknown, Unwrap<Schemas['ApiResponsePageResultRoleDTO']>>('/api/admin/role/list', params ?? {}),
  get: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseRoleDTO']>>('/api/admin/role/get', { id }),
  create: (data: Schemas['RoleCreateRequest']) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseRoleDTO']>>('/api/admin/role/create', data),
  update: (data: Schemas['RoleUpdateRequest']) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/role/update', data),
  delete: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/role/delete', { id }),
  assignToUser: (userId: number, roleId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/role/assign', { userId, roleId }),
  removeFromUser: (userId: number, roleId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/role/remove', { userId, roleId }),
};
