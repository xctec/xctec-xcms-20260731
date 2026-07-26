import { http } from './http';

export const operationApi = {
  getDashboard: () => http.post('/admin/operation/dashboard', {}),
  getTenantMetrics: (tenantId: number) => http.post('/admin/operation/tenant-metrics', { tenantId }),
  getQuotaReport: (params: { page: number; size: number }) => http.post('/admin/operation/quota-report', params),
  getAlertRules: () => http.post('/admin/operation/alert-rules', {}),
};
