import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const taskApi = {
  list: (params: { page: number; size: number }) =>
    http.post<unknown, Unwrap<Schemas['ApiResponsePageResultTaskScheduleDTO']>>('/admin/task/list', params),
  create: (data: Schemas['TaskCreateRequest']) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/task/create', data),
  pause: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/task/pause', { id }),
  resume: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/task/resume', { id }),
  delete: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/admin/task/delete', { id }),
};
