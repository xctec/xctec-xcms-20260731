import type { Schemas } from '@/types/api-helpers';

/**
 * 权限 DTO：直接复用 OpenAPI 生成的契约类型（ApiResponseListPermissionDTO.data 的元素类型）。
 */
export type PermissionDTO = Schemas['PermissionDTO'];

/**
 * 角色 DTO：基础字段复用生成契约类型（ApiResponseListRoleDTO / ApiResponseRoleDTO 的元素类型）；
 * permCount / userCount / dataScope 为列表展示用的聚合字段，由后端统计/数据范围接口另行提供，
 * 生成契约中不含，这里以交叉类型补充。
 */
export type RoleDTO = Schemas['RoleDTO'] & {
  permCount?: number;
  userCount?: number;
  dataScope?: DataScope;
};

/**
 * 角色数据范围：对应 /admin/authz/data-scope 返回结构。
 * 后端未为此单独建模（api-generated 无 ApiResponseDataScopeDTO），前端本地定义。
 */
export interface DataScope {
  scopeType?: string;
  scopeValues?: string[];
}
