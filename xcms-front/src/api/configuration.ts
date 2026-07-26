import { http } from './http';

export const configApi = {
  getParam: (paramKey: string) => http.post('/admin/config/param/get', { paramKey }),
  listParams: () => http.post('/admin/config/param/list', {}),
  getDictionaries: (dictType?: string) => http.post('/admin/config/dict/list', { dictType }),
  getFeatureConfig: (tenantId: number) => http.post('/admin/config/feature/get', { tenantId }),
  toggleFeature: (tenantId: number, featureCode: string, enabled: boolean) => http.post('/admin/config/feature/toggle', { tenantId, featureCode, enabled }),
};
