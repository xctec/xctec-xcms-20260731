import { http } from './http';

export const auditApi = {
  list: (params: { page: number; size: number; module?: string; action?: string }) => http.post('/admin/audit/list', params),
  export: (params: Record<string, unknown>) => http.post('/admin/audit/export', params),
};
