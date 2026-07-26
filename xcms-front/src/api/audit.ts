import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const auditApi = {
  list: (params: Schemas['AuditQuery']) =>
    http.post<unknown, Unwrap<Schemas['ApiResponsePageResultAuditLogDTO']>>('/admin/audit/list', params),
  export: (params: Record<string, unknown>) =>
    http.post('/admin/audit/export', params, { responseType: 'blob' }),
};
