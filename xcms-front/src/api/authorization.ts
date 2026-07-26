import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const authzApi = {
  getRolePermissions: (roleId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseListPermissionDTO']>>('/admin/authz/role-permissions', { roleId }),
  assignPermissions: (roleId: number, permissionIds: number[]) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/authz/assign', { roleId, permissionIds }),
  getDataScope: (roleId: number) => http.post('/admin/authz/data-scope', { roleId }),
  updateDataScope: (roleId: number, scopeType: string, scopeValues: string[]) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/authz/update-scope', { roleId, scopeType, scopeValues }),
};
