import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const configApi = {
  getParam: (paramKey: string) => http.post('/api/admin/config/param/get', { paramKey }),
  listParams: () => http.post('/api/admin/config/param/list', {}),
  getDictionaries: (dictType?: string) => http.post('/api/admin/config/dict/list', { dictType }),
  getFeatureConfig: (tenantId: number) => http.post('/api/admin/config/feature/get', { tenantId }),
  toggleFeature: (tenantId: number, featureCode: string, enabled: boolean) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/admin/config/feature/toggle', { tenantId, featureCode, enabled }),
};
