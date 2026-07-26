import { http } from './http';

export const authzApi = {
  getRolePermissions: (roleId: number) => http.post('/admin/authz/role-permissions', { roleId }),
  assignPermissions: (roleId: number, permissionIds: number[]) => http.post('/admin/authz/assign', { roleId, permissionIds }),
  getDataScope: (roleId: number) => http.post('/admin/authz/data-scope', { roleId }),
  updateDataScope: (roleId: number, scopeType: string, scopeValues: string[]) => http.post('/admin/authz/update-scope', { roleId, scopeType, scopeValues }),
};
