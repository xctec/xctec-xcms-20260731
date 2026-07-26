import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const orgApi = {
  getDeptTree: () => http.post<unknown, Unwrap<Schemas['ApiResponseListDepartmentTreeDTO']>>('/admin/org/dept/tree', {}),
  getDept: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseDepartmentDTO']>>('/admin/org/dept/get', { id }),
  createDept: (data: Record<string, unknown>) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/org/dept/create', data),
  updateDept: (data: Record<string, unknown>) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/org/dept/update', data),
  deleteDept: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/org/dept/delete', { id }),
  moveDept: (deptId: number, newParentId: number | null) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/org/dept/move', { deptId, newParentId }),

  getPositions: (deptId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseListUserPositionDTO']>>('/admin/org/position/list', { deptId }),
  createPosition: (data: Record<string, unknown>) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/org/position/create', data),
  deletePosition: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/org/position/delete', { id }),

  getUserGroups: () => http.post<unknown, Unwrap<Schemas['ApiResponseListUserGroupDTO']>>('/admin/org/group/list', {}),
  createUserGroup: (data: Record<string, unknown>) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/org/group/create', data),
  deleteGroup: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/org/group/delete', { id }),
  addGroupMembers: (groupId: number, userIds: number[]) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/org/group/add-members', { groupId, userIds }),
  removeGroupMember: (groupId: number, userId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/org/group/remove-member', { groupId, userId }),
};
