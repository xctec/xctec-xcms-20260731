import { http } from './http';
import type { UserDTO, RoleDTO } from '@/types/identity';
import type { PageResult } from '@/types/common';

export const userApi = {
  list: (params: { page: number; size: number; keyword?: string }) =>
    http.post<unknown, PageResult<UserDTO>>('/admin/user/list', params),
  get: (id: number) => http.post<unknown, UserDTO>('/admin/user/get', { id }),
  create: (data: Partial<UserDTO>) => http.post<unknown, UserDTO>('/admin/user/create', data),
  update: (data: Partial<UserDTO>) => http.post<unknown, void>('/admin/user/update', data),
  delete: (id: number) => http.post<unknown, void>('/admin/user/delete', { id }),
  resetPassword: (id: number) => http.post<unknown, void>('/admin/user/reset-password', { id }),
};

export const roleApi = {
  list: () => http.post<unknown, RoleDTO[]>('/admin/role/list', {}),
  get: (id: number) => http.post<unknown, RoleDTO>('/admin/role/get', { id }),
  create: (data: Partial<RoleDTO>) => http.post<unknown, RoleDTO>('/admin/role/create', data),
  update: (data: Partial<RoleDTO>) => http.post<unknown, void>('/admin/role/update', data),
  delete: (id: number) => http.post<unknown, void>('/admin/role/delete', { id }),
  assignToUser: (userId: number, roleId: number) => http.post<unknown, void>('/admin/role/assign', { userId, roleId }),
  removeFromUser: (userId: number, roleId: number) => http.post<unknown, void>('/admin/role/remove', { userId, roleId }),
};
