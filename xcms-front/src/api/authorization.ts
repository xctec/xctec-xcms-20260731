import { http } from '@/api/http';
import { Schemas, Unwrap } from '@/types/api-helpers';
import type { PermissionDTO } from '@/types/authorization';

export type DataRuleDTO = Schemas['DataRuleDTO'];
export type DataRuleCreateRequest = Schemas['DataRuleCreateRequest'];

/**
 * 角色权限 API：对齐后端 RolePermissionController（/admin/role-permission/*，全 POST）。
 * - get 使用 IdRequest{id}（非 roleId），返回 ApiResponse<List<PermissionDTO>>
 * - assign 使用 RolePermissionAssignRequest{roleId, permissions: PermissionAssignRequest[]}
 */
export const authzApi = {
  getRolePermissions: (roleId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseListPermissionDTO']>>('/admin/role-permission/get', { id: roleId }),
  // AT-19：数据范围能力对接真实的 /admin/data-rule/* 端点（DataRuleController），
  // 不再指向臆造的 /admin/authz/data-scope。预设范围经 dataRuleApi 映射为规则 CRUD + 角色绑定。
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

/**
 * 数据规则 API（AT-19）：对齐后端 DataRuleController（/admin/data-rule/*，全 POST）。
 * 角色"数据范围"预设由前端映射为规则 CRUD + 绑定（见 Permission.tsx）。
 */
export const dataRuleApi = {
  list: (resourceType: string): Promise<DataRuleDTO[]> =>
    http.post<unknown, Unwrap<Schemas['ApiResponseListDataRuleDTO']>>('/admin/data-rule/list', { resourceType }),
  create: (data: DataRuleCreateRequest): Promise<DataRuleDTO> =>
    http.post<unknown, Unwrap<Schemas['ApiResponseDataRuleDTO']>>('/admin/data-rule/create', data),
  delete: (id: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/data-rule/delete', { id }),
  // 后端 DataRuleBindRequest 以 id 表示规则 ID
  bind: (ruleId: number, roleId: number, scopeValue?: string) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/data-rule/bind', { id: ruleId, roleId, scopeValue }),
  unbind: (ruleId: number, roleId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/data-rule/unbind', { id: ruleId, roleId }),
};
