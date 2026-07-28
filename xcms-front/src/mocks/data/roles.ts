import type { RoleDTO, PermissionDTO } from '@/types/authorization';

export const mockRoles: RoleDTO[] = [
  { id: 1, roleCode: 'SYSTEM_ADMIN', roleName: '系统管理员', roleType: 'SYSTEM', description: '超级管理员，拥有全部权限', parentId: 0, status: 'ACTIVE' },
  { id: 2, roleCode: 'TENANT_ADMIN', roleName: '租户管理员', roleType: 'TENANT', description: '管理租户内资源', parentId: 0, status: 'ACTIVE' },
  { id: 3, roleCode: 'NORMAL_USER', roleName: '普通用户', roleType: 'CUSTOM', description: '普通业务用户', parentId: 0, status: 'ENABLED' },
];

export const mockPermissions: PermissionDTO[] = [
  { id: 1, permCode: 'user:view', permName: '查看用户', permType: 'MENU', module: '用户管理', action: 'read' },
  { id: 2, permCode: 'user:create', permName: '新建用户', permType: 'BUTTON', module: '用户管理', action: 'write' },
  { id: 3, permCode: 'user:edit', permName: '编辑用户', permType: 'BUTTON', module: '用户管理', action: 'write' },
  { id: 4, permCode: 'user:delete', permName: '删除用户', permType: 'BUTTON', module: '用户管理', action: 'delete' },
  { id: 5, permCode: 'org:view', permName: '查看组织', permType: 'MENU', module: '组织架构', action: 'read' },
  { id: 6, permCode: 'org:edit', permName: '编辑组织', permType: 'BUTTON', module: '组织架构', action: 'write' },
  { id: 7, permCode: 'role:assign', permName: '分配角色', permType: 'BUTTON', module: '权限管理', action: 'write' },
  { id: 8, permCode: 'role:permission', permName: '分配权限', permType: 'BUTTON', module: '权限管理', action: 'write' },
];

/** 角色 -> 权限 ID 映射 */
export const mockRolePermissions: Record<number, number[]> = {
  1: [1, 2, 3, 4, 5, 6, 7, 8],
  2: [1, 2, 3, 4, 5, 6],
  3: [1, 5],
};

/** 数据规则（AT-19：对齐后端 DataRuleDTO，preset 规则由 Permission 页动态创建） */
export const mockDataRules: Array<{
  id?: number;
  ruleName?: string;
  ruleType?: string;
  resourceType?: string;
  dimension?: string;
  ruleConfig?: string;
  status?: string;
}> = [];

/** 数据规则 -> 角色绑定 */
export const mockDataRuleBindings: Array<{ ruleId: number; roleId: number }> = [];

let dataRuleIdSeq = 1;
export const nextDataRuleId = () => dataRuleIdSeq++;
