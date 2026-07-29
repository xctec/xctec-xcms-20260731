import { http } from './http';
import type { Schemas, Unwrap } from '@/types/api-helpers';

type S = Schemas;

export const tenantApi = {
  list: (params: S['TenantListChildrenRequest']) =>
    http.post<unknown, Unwrap<S['ApiResponsePageResultTenantDTO']>>(
      '/api/admin/tenant/list-children',
      params,
    ),
  create: (data: S['TenantCreateRequest']) =>
    http.post<unknown, Unwrap<S['ApiResponseTenantDTO']>>('/api/admin/tenant/create', data),
  update: (data: S['TenantUpdateRequest']) =>
    http.post<unknown, Unwrap<S['ApiResponseTenantDTO']>>('/api/admin/tenant/update', data),
  get: (id: number) =>
    http.post<unknown, Unwrap<S['ApiResponseTenantDTO']>>('/api/admin/tenant/get', { id }),
  delete: (id: number) =>
    http.post<unknown, Unwrap<S['ApiResponseVoid']>>('/api/admin/tenant/delete', { id }),
  getTree: () =>
    http.post<unknown, Unwrap<S['ApiResponseListTenantTreeDTO']>>('/api/admin/tenant/tree', {}),
  /** 登录前查找租户（免鉴权，登录页选择租户用） */
  lookup: (keyword?: string) =>
    http.post<unknown, Unwrap<S['ApiResponseListTenantLookupDTO']>>('/api/tenant/lookup', { keyword }),
};
