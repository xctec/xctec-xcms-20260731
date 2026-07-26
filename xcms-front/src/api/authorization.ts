import { http } from '@/api/http';
import { Schemas, Unwrap } from '@/types/api-helpers';
import type { PermissionDTO, RoleDTO, DataScope } from '@/types/authorization';

export const authzApi = {
  getRolePermissions: (roleId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseListPermissionDTO']>>('/admin/authz/role-permissions', { roleId }),
  // 数据范围接口返回结构见 DataScope；api-generated 无对应 ApiResponse 生成类型，直接声明返回 DataScope
  getDataScope: (roleId: number): Promise<DataScope> =>
    http.post<unknown, DataScope>('/admin/authz/data-scope', { roleId }),
  updateDataScope: (roleId: number, scopeType: string, scopeValues: string[]) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/authz/data-scope/update', { roleId, scopeType, scopeValues }),
  listPermissions: (): Promise<PermissionDTO[]> =>
    http.post<unknown, Unwrap<Schemas['ApiResponseListPermissionDTO']>>('/admin/authz/permissions', {}),
  assignPermissions: (roleId: number, permissionIds: number[]) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/authz/permissions/assign', { roleId, permissionIds }),
};
