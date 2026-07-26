import { http } from '@/api/http';
import { Schemas, Unwrap } from '@/types/api-helpers';
import type { PermissionDTO, RoleDTO, DataScope } from '@/types/authorization';

/**
 * 角色权限 API：对齐后端 RolePermissionController（/admin/role-permission/*，全 POST）。
 * - get 使用 IdRequest{id}（非 roleId），返回 ApiResponse<List<PermissionDTO>>
 * - assign 使用 RolePermissionAssignRequest{roleId, permissions: PermissionAssignRequest[]}
 */
export const authzApi = {
  getRolePermissions: (roleId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseListPermissionDTO']>>('/admin/role-permission/get', { id: roleId }),
  // 数据范围：后端暂无 data-scope 端点（能力落在 /admin/data-rule/*），接口形态待产品确认
  // data-scope 与 data-rule 的关系后，再决定前端改对接 data-rule 还是后端补 data-scope。
  // api-generated 无对应 ApiResponse 生成类型，返回结构本地定义见 DataScope。
  getDataScope: (roleId: number): Promise<DataScope> =>
    http.post<unknown, DataScope>('/admin/authz/data-scope', { roleId }),
  updateDataScope: (roleId: number, scopeType: string, scopeValues: string[]) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/authz/data-scope/update', { roleId, scopeType, scopeValues }),
  // TODO(后端待补): PermissionController 目前仅 user-permissions/check/menus，无「列举全部权限」接口。
  // 待后端补充（建议 /admin/permission/list，返回 ApiResponse<List<PermissionDTO>>）后，URL 与返回类型需同步对齐。
  // 当前保留 mock 端点 /admin/authz/permissions 以便开发联调。
  listPermissions: (): Promise<PermissionDTO[]> =>
    http.post<unknown, Unwrap<Schemas['ApiResponseListPermissionDTO']>>('/admin/authz/permissions', {}),
  assignPermissions: (roleId: number, permissionIds: number[]) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/role-permission/assign', {
      roleId,
      permissions: permissionIds.map((id) => ({ permId: id })),
    }),
};
