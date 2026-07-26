import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const fileApi = {
  upload: (file: File, folderId?: number) => {
    const formData = new FormData();
    formData.append('file', file);
    if (folderId) formData.append('folderId', String(folderId));
    return http.post<unknown, Unwrap<Schemas['ApiResponseFileDTO']>>('/api/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  delete: (id: number) => http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/file/delete', { id }),
  list: (params: { folderId?: number; page: number; size: number }) =>
    http.post<unknown, Unwrap<Schemas['ApiResponsePageResultFileDTO']>>('/api/file/list', params),
};
