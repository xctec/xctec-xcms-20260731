import { http } from './http';
import type { TenantDTO, TenantCreateRequest, TenantListRequest } from '@/types/tenant';
import type { PageResult } from '@/types/common';

export const tenantApi = {
  list: (params: TenantListRequest) =>
    http.post<unknown, PageResult<TenantDTO>>('/admin/tenant/list-children', params),
  create: (data: TenantCreateRequest) =>
    http.post<unknown, TenantDTO>('/admin/tenant/create', data),
  get: (id: number) => http.post<unknown, TenantDTO>('/admin/tenant/get', { id }),
  delete: (id: number) => http.post<unknown, void>('/admin/tenant/delete', { id }),
  getTree: () => http.post<unknown, TenantDTO[]>('/admin/tenant/tree', {}),
};
