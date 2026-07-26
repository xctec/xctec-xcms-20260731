import { http } from './http';

export const messageApi = {
  list: (params: { page: number; size: number; msgType?: string }) => http.post('/api/message/list', params),
  read: (ids: number[]) => http.post('/api/message/read', { ids }),
  delete: (ids: number[]) => http.post('/api/message/delete', { ids }),
};
