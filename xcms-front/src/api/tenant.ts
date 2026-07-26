import { http } from './http';
import type { Schemas, Unwrap } from '@/types/api-helpers';

type S = Schemas;

export const tenantApi = {
  list: (params: S['TenantListChildrenRequest']) =>
    http.post<unknown, Unwrap<S['ApiResponsePageResultTenantDTO']>>(
      '/admin/tenant/list-children',
      params,
    ),
  create: (data: S['TenantCreateRequest']) =>
    http.post<unknown, Unwrap<S['ApiResponseTenantDTO']>>('/admin/tenant/create', data),
  get: (id: number) =>
    http.post<unknown, Unwrap<S['ApiResponseTenantDTO']>>('/admin/tenant/get', { id }),
  delete: (id: number) =>
    http.post<unknown, Unwrap<S['ApiResponseVoid']>>('/admin/tenant/delete', { id }),
  getTree: () =>
    http.post<unknown, Unwrap<S['ApiResponseListTenantTreeDTO']>>('/admin/tenant/tree', {}),
  /** 登录前查找租户（免鉴权，登录页选择租户用） */
  // TODO: 后端重启后重导 openapi.json + gen:api，改回 Unwrap<S['ApiResponseListTenantLookupDTO']>
  lookup: (keyword?: string) =>
    http.post<unknown, { id: number; tenantCode: string; tenantName: string }[]>(
      '/api/tenant/lookup',
      { keyword },
    ),
};
