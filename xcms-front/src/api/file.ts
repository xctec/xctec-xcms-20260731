import { http } from './http';

export const fileApi = {
  upload: (file: File, folderId?: number) => {
    const formData = new FormData();
    formData.append('file', file);
    if (folderId) formData.append('folderId', String(folderId));
    return http.post('/api/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
  },
  delete: (id: number) => http.post('/api/file/delete', { id }),
  list: (params: { folderId?: number; page: number; size: number }) => http.post('/api/file/list', params),
};
