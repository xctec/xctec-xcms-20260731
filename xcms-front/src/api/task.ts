import { http } from './http';

export const taskApi = {
  list: (params: { page: number; size: number }) => http.post('/admin/task/list', params),
  create: (data: Record<string, unknown>) => http.post('/admin/task/create', data),
  pause: (id: number) => http.post('/admin/task/pause', { id }),
  resume: (id: number) => http.post('/admin/task/resume', { id }),
  delete: (id: number) => http.post('/admin/task/delete', { id }),
};
