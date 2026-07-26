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
  // 后端已补：POST /admin/permission/list 返回 ApiResponse<List<PermissionDTO>>（见 PermissionController.listAllPermissions）
  listPermissions: (): Promise<PermissionDTO[]> =>
    http.post<unknown, Unwrap<Schemas['ApiResponseListPermissionDTO']>>('/admin/permission/list', {}),
  // 权限项跨表（操作权限表 + 菜单表）id 可能重复，故 assign 需同时带 permType，
  // 后端据 (permId, permType) 精确落库，避免仅凭 id 误判权限来源。
  assignPermissions: (roleId: number, permissions: { permId: number; permType?: string }[]) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/role-permission/assign', {
      roleId,
      permissions,
    }),
};
