import { http } from './http';

export const portalApi = {
  getWorkbench: () => http.post('/api/portal/workbench', {}),
  getTodoList: (params: { page: number; size: number }) => http.post('/api/portal/todo-list', params),
  getNotifications: (params: { page: number; size: number }) => http.post('/api/portal/notifications', params),
};
