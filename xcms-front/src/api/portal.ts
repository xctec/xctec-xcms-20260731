import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const portalApi = {
  getWorkbench: () => http.post<unknown, Unwrap<Schemas['ApiResponsePortalWorkbenchDTO']>>('/api/portal/workbench', {}),
  getTodoList: (params: { page: number; size: number }) => http.post('/api/portal/todo-list', params),
  getNotifications: (params: { page: number; size: number }) => http.post('/api/portal/notifications', params),
};
