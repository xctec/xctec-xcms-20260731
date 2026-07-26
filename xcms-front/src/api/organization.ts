import { http } from './http';

export const orgApi = {
  getDeptTree: () => http.post('/admin/org/dept/tree', {}),
  getDept: (id: number) => http.post('/admin/org/dept/get', { id }),
  createDept: (data: Record<string, unknown>) => http.post('/admin/org/dept/create', data),
  updateDept: (data: Record<string, unknown>) => http.post('/admin/org/dept/update', data),
  deleteDept: (id: number) => http.post('/admin/org/dept/delete', { id }),
  moveDept: (deptId: number, newParentId: number | null) => http.post('/admin/org/dept/move', { deptId, newParentId }),

  getPositions: (deptId: number) => http.post('/admin/org/position/list', { deptId }),
  createPosition: (data: Record<string, unknown>) => http.post('/admin/org/position/create', data),
  deletePosition: (id: number) => http.post('/admin/org/position/delete', { id }),

  getUserGroups: () => http.post('/admin/org/group/list', {}),
  createUserGroup: (data: Record<string, unknown>) => http.post('/admin/org/group/create', data),
  deleteGroup: (id: number) => http.post('/admin/org/group/delete', { id }),
  addGroupMembers: (groupId: number, userIds: number[]) => http.post('/admin/org/group/add-members', { groupId, userIds }),
  removeGroupMember: (groupId: number, userId: number) => http.post('/admin/org/group/remove-member', { groupId, userId }),
};
