import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const orgApi = {
  getDeptTree: () => http.post<unknown, Unwrap<Schemas['ApiResponseListDepartmentTreeDTO']>>('/api/admin/org/dept/tree', {}),
  getDept: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseDepartmentDTO']>>('/api/admin/org/dept/get', { id }),
  createDept: (data: Schemas['DepartmentCreateRequest']) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/org/dept/create', data),
  updateDept: (data: Schemas['DepartmentUpdateRequest']) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/org/dept/update', data),
  deleteDept: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/org/dept/delete', { id }),
  moveDept: (deptId: number, newParentId: number | null) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/org/dept/move', { deptId, newParentId }),

  getPositions: (deptId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseListUserPositionDTO']>>('/api/admin/org/position/list', { deptId }),
  createPosition: (data: Schemas['PositionCreateRequest']) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/org/position/create', data),
  deletePosition: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/org/position/delete', { id }),

  getUserGroups: () => http.post<unknown, Unwrap<Schemas['ApiResponseListUserGroupDTO']>>('/api/admin/org/group/list', {}),
  createUserGroup: (data: Schemas['UserGroupCreateRequest']) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/org/group/create', data),
  deleteGroup: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/org/group/delete', { id }),
  addGroupMembers: (groupId: number, userIds: number[]) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/org/group/add-members', { groupId, userIds }),
  removeGroupMember: (groupId: number, userId: number) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/org/group/remove-member', { groupId, userId }),
};
