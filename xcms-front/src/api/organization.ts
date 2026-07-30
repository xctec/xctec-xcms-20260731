import { http } from '@/api/http';
import type { Schemas } from '@/types/api-helpers';

export const orgApi = {
  // 部门（对齐后端 DepartmentController: /api/admin/department）
  getDeptTree: () =>
    http.post<Schemas['DepartmentTreeDTO'][]>('/api/admin/department/tree').then((r) => r.data),
  getDept: (id: number) =>
    http.post<Schemas['DepartmentDTO']>('/api/admin/department/get', { id }).then((r) => r.data),
  createDept: (data: Schemas['DepartmentCreateRequest']) =>
    http.post<Schemas['DepartmentDTO']>('/api/admin/department/create', data).then((r) => r.data),
  updateDept: (data: Schemas['DepartmentUpdateRequest']) =>
    http.post<Schemas['DepartmentDTO']>('/api/admin/department/update', data).then((r) => r.data),
  deleteDept: (id: number) =>
    http.post<void>('/api/admin/department/delete', { id }).then((r) => r.data),
  // targetId 可空：支持把部门移动到根级（后端 MoveRequest.targetId 为 Long 可空）
  moveDept: (deptId: number, newParentId: number | null) =>
    http
      .post<void>('/api/admin/department/move', { id: deptId, targetId: newParentId ?? null })
      .then((r) => r.data),

  // 岗位（对齐后端 PositionController: /api/admin/position）
  // 返回类型与 Organization.tsx 的 `as UserPositionDTO[]` 及 deptName/positionName 列保持一致
  getPositions: (deptId: number) =>
    http.post<Schemas['UserPositionDTO'][]>('/api/admin/position/list-by-dept', { id: deptId }).then((r) => r.data),
  createPosition: (data: Schemas['PositionCreateRequest']) =>
    http.post<Schemas['PositionDTO']>('/api/admin/position/create', data).then((r) => r.data),
  deletePosition: (id: number) =>
    http.post<void>('/api/admin/position/delete', { id }).then((r) => r.data),

  // 用户组（对齐后端 UserGroupController: /api/admin/user-group）
  getUserGroups: () =>
    http.post<Schemas['UserGroupDTO'][]>('/api/admin/user-group/list').then((r) => r.data),
  createUserGroup: (data: Schemas['UserGroupCreateRequest']) =>
    http.post<Schemas['UserGroupDTO']>('/api/admin/user-group/create', data).then((r) => r.data),
  deleteGroup: (id: number) =>
    http.post<void>('/api/admin/user-group/delete', { id }).then((r) => r.data),
  addGroupMembers: (groupId: number, userIds: number[]) =>
    http
      .post<void>('/api/admin/user-group/add-members', { id: groupId, memberIds: userIds })
      .then((r) => r.data),
  removeGroupMember: (groupId: number, userId: number) =>
    http
      .post<void>('/api/admin/user-group/remove-members', { id: groupId, memberIds: [userId] })
      .then((r) => r.data),
};
