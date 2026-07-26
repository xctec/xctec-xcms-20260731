import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const operationApi = {
  getDashboard: () => http.post<unknown, Unwrap<Schemas['ApiResponseDashboardDTO']>>('/admin/operation/dashboard', {}),
  getTenantMetrics: (tenantId: number) => http.post('/admin/operation/tenant-metrics', { tenantId }),
  getQuotaReport: (params: { page: number; size: number }) => http.post('/admin/operation/quota-report', params),
  getAlertRules: () => http.post('/admin/operation/alert-rules', {}),
};
