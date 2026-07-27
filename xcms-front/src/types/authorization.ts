import type { Schemas } from '@/types/api-helpers';

/**
 * 权限 DTO：直接复用 OpenAPI 生成的契约类型（ApiResponseListPermissionDTO.data 的元素类型）。
 */
export type PermissionDTO = Schemas['PermissionDTO'];

/**
 * 角色 DTO：基础字段复用生成契约类型（ApiResponseListRoleDTO / ApiResponseRoleDTO 的元素类型）；
 * permCount / userCount 为列表展示用的聚合字段，由后端统计接口另行提供，
 * 生成契约中不含，这里以交叉类型补充。
 * （AT-19：数据范围改为对接 /admin/data-rule/*，DataScope 本地类型已移除）
 */
export type RoleDTO = Schemas['RoleDTO'] & {
  permCount?: number;
  userCount?: number;
};
