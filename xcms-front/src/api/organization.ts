import { http } from '@/api/http';

export const orgApi = {
  // 部门（对齐后端 DepartmentController: /api/admin/department）
  getDeptTree: () =>
    http.post<DepartmentTreeDTO[]>('/api/admin/department/tree').then((r) => r.data),
  getDept: (id: number) =>
    http.post<DepartmentTreeDTO>('/api/admin/department/get', { id }).then((r) => r.data),
  createDept: (data: DepartmentCreateDTO) =>
    http.post<DepartmentTreeDTO>('/api/admin/department/create', data).then((r) => r.data),
  updateDept: (data: DepartmentUpdateDTO) =>
    http.post<DepartmentTreeDTO>('/api/admin/department/update', data).then((r) => r.data),
  deleteDept: (id: number) =>
    http.post<void>('/api/admin/department/delete', { id }).then((r) => r.data),
  moveDept: (deptId: number, newParentId: number) =>
    http
      .post<void>('/api/admin/department/move', { id: deptId, targetId: newParentId })
      .then((r) => r.data),

  // 岗位（对齐后端 PositionController: /api/admin/position）
  getPositions: (deptId: number) =>
    http.post<UserPositionDTO[]>('/api/admin/position/list-by-dept', { id: deptId }).then((r) => r.data),
  createPosition: (data: PositionCreateDTO) =>
    http.post<UserPositionDTO>('/api/admin/position/create', data).then((r) => r.data),
  deletePosition: (id: number) =>
    http.post<void>('/api/admin/position/delete', { id }).then((r) => r.data),

  // 用户组（对齐后端 UserGroupController: /api/admin/user-group）
  getUserGroups: () =>
    http.post<UserGroupDTO[]>('/api/admin/user-group/list').then((r) => r.data),
  createUserGroup: (data: UserGroupCreateDTO) =>
    http.post<UserGroupDTO>('/api/admin/user-group/create', data).then((r) => r.data),
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
