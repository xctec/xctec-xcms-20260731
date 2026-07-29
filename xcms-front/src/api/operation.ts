import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const operationApi = {
  getDashboard: () => http.post<unknown, Unwrap<Schemas['ApiResponseDashboardDTO']>>('/api/admin/operation/dashboard', {}),
  getTenantMetrics: (tenantId: number) => http.post('/api/admin/operation/tenant-metrics', { tenantId }),
  getQuotaReport: (params: { page: number; size: number }) => http.post('/api/admin/operation/quota-report', params),
  getAlertRules: () => http.post('/api/admin/operation/alert-rules', {}),
};
