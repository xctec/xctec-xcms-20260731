import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const messageApi = {
  list: (params: Schemas['MessageQuery']) =>
    http.post<unknown, Unwrap<Schemas['ApiResponsePageResultMessageDTO']>>('/api/message/list', params),
  read: (ids: number[]) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/message/read', { ids }),
  delete: (ids: number[]) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/message/delete', { ids }),
};
