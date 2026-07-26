import { http } from './http';
import type { PermissionDTO, DataScope } from '@/types/authorization';

export const authzApi = {
  getRolePermissions: (roleId: number) =>
    http.post<unknown, PermissionDTO[]>('/admin/authz/role-permissions', { roleId }),
  assignPermissions: (roleId: number, permissionIds: number[]) =>
    http.post<unknown, void>('/admin/authz/assign', { roleId, permissionIds }),
  getDataScope: (roleId: number) => http.post<unknown, DataScope>('/admin/authz/data-scope', { roleId }),
  updateDataScope: (roleId: number, scopeType: string, scopeValues: string[]) =>
    http.post<unknown, void>('/admin/authz/update-scope', { roleId, scopeType, scopeValues }),
  /** 全部权限列表（用于分配权限时的勾选） */
  listPermissions: () => http.post<unknown, PermissionDTO[]>('/admin/authz/permissions', {}),
};
